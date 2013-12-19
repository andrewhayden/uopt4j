// Copyright (c) 2013 Andrew Hayden. All rights reserved.
// Use of this source code is governed by an Apache 2.0 license that can be
// found in the LICENSE.md file.

// A simple tool class that illustrates how to use uopt4j
public class ExampleTool {
    boolean verbose;
    String infile;
    String outfile;
    public void run() {
        // do really interesting stuff here.
    }

    public static void main(String... args) {
        MicroOptions options = new MicroOptions();
        options.option("v").describedAs("be verbose").isUnary();
        options.option("infile").describedAs("path of input file").isRequired();
        options.option("outfile").describedAs("write here instead of /tmp/out");
        try {
            options.parse(args);
        } catch (MicroOptions.OptionException e) {
            System.err.println("Usage:");
            System.err.println(options.usageString());
            System.exit(-1);
        }
        ExampleTool tool = new ExampleTool();
        tool.verbose = options.has("v");
        tool.infile = options.getArg("infile");
        tool.outfile = options.getArg("outfile", "/tmp/out");
        tool.run();
    }
}