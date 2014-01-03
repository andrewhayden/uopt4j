// Copyright (c) 2013 Andrew Hayden. All rights reserved.
// Use of this source code is governed by an Apache 2.0 license that can be
// found in the LICENSE.md file.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Non-rigorous half-baked Java code minifier. Always run unit tests after
 * minifying! Going beyond this probably requires a real parser.
 */
public class JavaMinifyTool {
    // Regex from: http://ostermiller.org/findcomment.html
    private final static String MULTI_LINE_COMMENT_REGEX = "/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/";

    public final static void main(String... args) throws Exception {
        MicroOptions options = new MicroOptions();
        options.option("in").isRequired().describedAs("path to the .java file to read");
        options.option("out").isRequired().describedAs("path to the .java file to write");
        options.option("mode").isRequired().describedAs("one of: [compact,min]: 'compact' just throws away blanks and comments; 'min' crunches the code down into an almost unreadable block.");
        options.option("wrap-at").describedAs("if mode=min, wrap lines after this many chars (default=80)");
        options.option("header").describedAs("optional header text to apply to generated files");
        try {
            options.parse(args);
        } catch (MicroOptions.OptionException e) {
            System.err.println("Usage:");
            System.err.println(options.usageString());
            System.exit(-1);
        }
        run(options.getArg("in"), options.getArg("out"), options.getArg("mode"),
                Integer.parseInt(options.getArg("wrap-at", "80")),
                options.getArg("header", ""));
    }

    private static void run(String in, String out, String mode, int wrapAt, String header) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(in));
        StringBuilder buffer = new StringBuilder();
        String currentLine = null;

        // Buffer and strip all single-line comments
        while((currentLine = reader.readLine()) != null) {
            // Trim trailing whitespace
            currentLine = currentLine.replaceFirst("\\s+$", "");

            // Delete single-line comments
            int indexOfComment = currentLine.indexOf("//");
            if (indexOfComment >= 0) {
                currentLine = currentLine.substring(0, indexOfComment);
                currentLine = currentLine.replaceFirst("\\s+$", "");
            }
            buffer.append(currentLine);
            buffer.append('\n');
        }

        // Remove multi-line comments
        Pattern pattern = Pattern.compile(MULTI_LINE_COMMENT_REGEX, Pattern.MULTILINE);
        buffer = new StringBuilder(pattern.matcher(buffer).replaceAll(""));

        // Strip empty lines
        pattern = Pattern.compile("^[\\s&&[^\\n]]*\n", Pattern.MULTILINE);
        buffer = new StringBuilder(pattern.matcher(buffer).replaceAll(""));

        if (mode.equals("min")) {
            minify(buffer, wrapAt);
        }
        PrintWriter writer = new PrintWriter(out);
        writer.println(header);
        writer.print(buffer);
        writer.flush();
        writer.close();
        reader.close();
    }

    /**
     * Minify the Java source code.
     * @param buffer the buffer to minify
     * @param wrapAt the column to wrap lines at
     */
    private static void minify(StringBuilder buffer, int wrapAt) {
        List<String> words = extractWords(buffer);
        buffer.setLength(0);
        Iterator<String> iterator = words.iterator();
        int lineLength = 0;
        int wordCount = 0;
        // TODO: Remove unnecessary spaces around things like '{', '(', etc.
        while(iterator.hasNext()) {
            wordCount++;
            String word = iterator.next();
            if (lineLength + 1 + word.length() > wrapAt) {
                // Wrap
                if (wordCount > 1) buffer.append('\n');
                buffer.append(word);
                lineLength = word.length();
            } else {
                if (lineLength > 0) {
                    lineLength++;
                    buffer.append(' ');
                }
                buffer.append(word);
                lineLength += word.length();
            }
        }
    }

    /**
     * Break the source into words.
     * @param buffer the buffer to extract words from
     * @return the words
     */
    private static List<String> extractWords(StringBuilder buffer) {
        List<String> words = new LinkedList<String>();
        final StringBuilder wordBuffer = new StringBuilder();
        boolean doubleQuoted = false;
        boolean singleQuoted = false;
        boolean collapsingWhitespace = false;
        int backslashCount=0;

        for (int x=0; x<buffer.length(); x++) {
            char c = buffer.charAt(x);
            boolean isWs = Character.isWhitespace(c);
            if (!singleQuoted && !doubleQuoted && collapsingWhitespace && !isWs) {
                // Output one space to collapse all preceding whitespace
                words.add(wordBuffer.toString());
                wordBuffer.setLength(0);
                collapsingWhitespace = false;
            }

            if (c == '\\') {
                backslashCount++;
                wordBuffer.append(c);
            } else {
                if (c == '"') {
                    if (backslashCount == 0 || backslashCount % 2 == 0) {
                        if (!singleQuoted) doubleQuoted = !doubleQuoted;
                    }
                    wordBuffer.append(c);
                } else if (c == '\'') {
                    if (backslashCount == 0 || backslashCount % 2 == 0) {
                        if (!doubleQuoted) singleQuoted = !singleQuoted;
                    }
                    wordBuffer.append(c);
                } else {
                    // Not a single or double quote char
                    if (singleQuoted || doubleQuoted) {
                        // We're inside quotes, always output everything
                        wordBuffer.append(c);
                    }
                    else if (!Character.isWhitespace(c)) {
                        // Non-whitespace char outside of quotes, always output
                        wordBuffer.append(c);
                    } else {
                        // else, it's a whitespace char outside of quotes.
                        // Begin collapse.
                        collapsingWhitespace = true;
                    }
                }
                backslashCount = 0;
            }
        }
        if (wordBuffer.length() > 0) words.add(wordBuffer.toString());
        return words;
    }
}
