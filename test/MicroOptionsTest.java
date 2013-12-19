// Copyright (c) 2013 Andrew Hayden. All rights reserved.
// Use of this source code is governed by a MIT-style license that can be
// found in the LICENSE.md file.
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
        String expected =
                " -a           A (optional)\n" +
                "--bb [ARG]    B (required)\n" +
                "--cc [ARG]    C (optional)\n" +
                " -d           (required)";
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

    @Test public void testValueFor_Fallback() {
        opts.option("x");
        opts.option("y");
        opts.parse("-y", "y-value");
        assertFalse(opts.has("x"));
        assertEquals("x-fallback", opts.getArg("x", "x-fallback"));
        assertEquals("y-value", opts.getArg("y", "y-fallback"));
    }

    @Test public void testValueFor_Unary() {
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
}