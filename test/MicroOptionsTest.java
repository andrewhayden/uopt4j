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
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MicroOptionsTest {
    private MicroOptions opts;
    @Before public void setup() { opts = new MicroOptions(); }

    @Test public void testUsageString() {
        assertEquals("", opts.usageString());
        // Testing order, padding, optional/required, and null/non-null descrip
        opts.option("cc").describedAs("C");
        opts.option("a").describedAs("A").isUnary();
        opts.option("bb").describedAs("B").isRequired();
        opts.option("d").isUnary().isRequired();
        opts.option("ee").isUnary().isRequired();
        String expected =
                " -a           A (optional)\n" +
                "--bb [ARG]    B (required)\n" +
                "--cc [ARG]    C (optional)\n" +
                " -d           (required)\n" +
                "--ee          (required)";
        assertEquals(expected, opts.usageString());
    }

    @Test public void testParse_Empty() {
        opts.parse(new String[]{});
    }

    @Test public void testParse_Simple() {
        opts.option("x").isUnary();
        opts.option("long");
        opts.parse("-x", "--long", "longarg");
        
        assertTrue(opts.has("x"));
        assertTrue(opts.has("long"));
        assertEquals("longarg", opts.getArg("long"));
        try {
            opts.has("y");
            fail("queried for an unconfigured option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testParse_BadSyntax() {
        opts.option("x").isUnary();
        try {
            opts.parse("articulate");
            fail("parsed an option without any hypens");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testParse_OnlyOneHyphenForShort() {
        opts.option("x").isUnary();
        try {
            opts.parse("--x");
            fail("parsed a short option with two hypens");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testParse_OnlyTwoHyphensForLong() {
        opts.option("long").isUnary();
        try {
            opts.parse("-long");
            fail("parsed a long option with a single hyphen");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testParse_RequiredOption() {
        opts.option("x").isRequired().isUnary();
        opts.option("y").isUnary();
        try {
            opts.parse("-y");
            fail("avoided passing required option");
        } catch (MicroOptions.RequiredOptionException e) {
            // Expected
        }
        opts.parse("-y", "-x");
        assertTrue(opts.has("x"));
        assertTrue(opts.has("y"));
    }

    @Test public void testParse_MissingArg() {
        opts.option("x").isRequired();
        try {
            opts.parse("-x");
            fail("parsed option with missing argument");
        } catch(MicroOptions.MissingArgException e) {
            // Expected
        }
    }

    @Test public void testParse_UnsupportedOption() {
        try {
            opts.parse("-x");
            fail("parsed unsupported arg!");
        } catch(MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testGetArg_Fallback() {
        opts.option("x");
        opts.option("y");
        opts.parse("-y", "y-value");
        assertFalse(opts.has("x"));
        assertEquals("x-fallback", opts.getArg("x", "x-fallback"));
        assertEquals("y-value", opts.getArg("y", "y-fallback"));
    }

    @Test public void testGetArg_Unary() {
        opts.option("x").isUnary();
        opts.parse("-x");
        assertTrue(opts.has("x"));
        try {
            opts.getArg("x");
            fail("Queried for an arg to a unary option");
        } catch (MicroOptions.OptionException e) {
            // Expected
        }
        try {
            opts.getArg("x", "foo");
            fail("Queried for an arg to a unary option");
        } catch (MicroOptions.OptionException e) {
            // Expected
        }
    }

    @Test public void testGetArg_Unsupported() {
        opts.option("a").isUnary();
        opts.option("b");
        opts.parse("-a", "-b", "b-value");
        try {
            opts.getArg("z", "foo");
            fail();
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testGetArg_Null() {
        opts.option("a").isUnary();
        opts.parse("-a");
        try {
            opts.getArg(null, "foo");
            fail("succeeded in attempt to get arg for null option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testHasArg_Null() {
        opts.option("a").isUnary();
        opts.parse("-a");
        try {
            opts.has(null);
            fail("succeeded in attempt to check null option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testGetArg_EmptyString() {
        opts.option("a").isUnary();
        opts.parse("-a");
        try {
            opts.getArg("");
            fail("succeeded in attempt to get arg for empty option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testHasArg_EmptyString() {
        opts.option("a").isUnary();
        opts.parse("-a");
        try {
            opts.has("");
            fail("succeeded in attempt to check empty option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testGetArg_IllegalString() {
        opts.option("a").isUnary();
        opts.parse("-a");
        try {
            opts.getArg("-a");
            fail("succeeded in attempt to get arg for illegal option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testHasArg_IllegalString() {
        opts.option("a").isUnary();
        opts.parse("-a");
        try {
            opts.has("-a");
            fail("succeeded in attempt to check illegal option");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testOption_EmptyString() {
        try {
            opts.option("");
            fail("Created option with empty string as the name");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testOption_NullString() {
        try {
            opts.option(null);
            fail("Created option with null string as the name");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }

    @Test public void testOption_IllegalString() {
        try {
            opts.option("-illegal-because-it-starts-with-a-hypen");
            fail("Created option illegal name");
        } catch (MicroOptions.UnsupportedOptionException e) {
            // Expected
        }
    }
}
