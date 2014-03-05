// Copyright 2014 Google Inc. All rights reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
