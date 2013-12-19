uopt4j
======
A micro framework for command-line options in Java, made to be copy-pasted into
standalone code.


Why? (The Short Version)
========================
Because sometimes the difficulty of adding a dependency upon a third-party
library isn't justified by the scope of your task but you want a tolerable
stopgap parser that makes things easy. uopt4j provides this. Just copy and
paste the whole source (or .class) into your project and go; you can move to a
different system later with little effort. For a longer explanation see the
section called "Why? (The Long Version)" at the end of this document.

Here are some reasons to use uopt4j:
* Apache license: Free to use, abuse, modify and redistribute
* Tiny footprint: One standalone source file, less than 100 lines of code
* Fully tested: 100% code coverage (see code in /test)
* Compatible with Java 1.5 or later runtime environments

Here are some reasons NOT to use uopt4j:
* You need support for complex argument types and/or custom argument parsers
* You want to just have things autowired via annotations
* You need to have robust support for list-style arguments or bare arguments



How to Use
==========
1. Consume the standalone class in src/MicroOptions.java however you wish:
   (A) Paste it straight into the class that needs it
   (B) Dump it at the root of your source tree (in the default package)
   (C) Paste it somewhere else and add an appropriate "package" directive
   (D) Copy the JAR from /release and add it to your classpath
2. Configure options.
3. Call MicroOptions.parse(String... args)
3. Consume parsed options and arguments as appropriate for your tool

PROTIP: These smaller variants of the source may be used instead as desired:
* altsrc-compact/MicroOptions.java: no comments or blank lines, but readable
* altsrc-minified/MicroOptions.java: very very short blob of code



Example Usage
=============

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



Example Output
==============
    Usage:
    --infile  [ARG]    path of input file (required)
    --outfile [ARG]    write here instead of /tmp/out (optional)
     -v                be verbose (optional)



Feature Set
===========
uopt4j focuses on a small set of critical features and leaves the rest to you.
The features deemed critical may be summarized as follows:
* Options can be required or optional, with or without arguments
* Provides pretty-formatted usage string with alignment, argument and
  optional/required indicators
* Support for short (e.g., "-v") and long (e.g., "--verbose") style args.
  This support is automatic; single-char options are accessed with a single
  hyphen prefix, everything else with a double hyphen.

Any missing functionality (e.g., converting arguments from Strings to other
formats) is left to the caller. This keeps things clean and compact.



Why? (The Long Version)
=======================
You're hacking on some arbitrary Java tool and suddenly find yourself wanting
to configure its behavior with a few simple command line switches. Your mind
immediately jumps to thoughts of libraries like Args4j et al, but then you
bring up StackOverflow and find threads like this:

  http://stackoverflow.com/questions/367706

... with links to depressingly long lists of frameworks such as this:

  http://jewelcli.lexicalscope.com/related.html

... which pretty much all look like they'll require adding libraries to your
code base. You remember that you'll need permission to bring in a third-party
library and reconfigure half the build system in order to add yet another JAR
to the classpath. Never one to waste time, you can't justify the time spent
messing with all that red tape just to continue hacking on your tool. Sighing
in resignation, you crack open your "main" function and reluctantly hard-code
an array of args. Ten minutes later you've decided that you need at least one
of the args to be optional, so you replace your array with a loop and add more
logic. It looks horrible but it works. Two days later you decide that everyone
ever born would benefit tremendously if they could just use your tool, but your
own mother would be ashamed of your shoddy command line parser. Should you bite
the bullet and get permission to add a library dependency to your project, or
clean up the nuclear waste in "main" as best you can? Both options are going
to take a while. Sigh. If only you had something that was "good enough" to
hand to your colleagues without having to lace your sharing messages with
caveats about the poor code quality...