/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

/*
 * @test
 * @summary Basic test for shared strings
 * Feature support: G1GC only, compressed oops/kptrs, 64-bit os, not on windows
 * @requires (sun.arch.data.model != "32") & (os.family != "windows")
 * @requires vm.cds
 * @requires vm.gc.G1
 * @library /test/hotspot/jtreg/runtime/appcds /test/lib
 * @modules java.base/jdk.internal.misc
 * @modules java.management
 *          jdk.jartool/sun.tools.jar
 * @build HelloString
 * @run main SharedStringsBasic
 */
import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;

// This test does not use SharedStringsUtils intentionally:
// - in order to demonstrate the basic use of the functionality
// - to provide sanity check and catch potential problems in the utils
public class SharedStringsBasic {
    public static void main(String[] args) throws Exception {
        String appJar = JarBuilder.build("SharedStringsBasic", "HelloString");

        String sharedArchiveConfigFile =
            TestCommon.getSourceFile("SharedStringsBasic.txt").toString();

        ProcessBuilder dumpPb = ProcessTools.createJavaProcessBuilder(true,
          TestCommon.makeCommandLineForAppCDS(
            "-XX:+UseAppCDS",
            "-XX:+UseCompressedOops",
            "-XX:+UseG1GC",
            "-cp", appJar,
            "-XX:SharedArchiveConfigFile=" + sharedArchiveConfigFile,
            "-XX:SharedArchiveFile=./SharedStringsBasic.jsa",
            "-Xshare:dump",
            "-Xlog:cds,cds+hashtables"));

        TestCommon.executeAndLog(dumpPb, "dump")
            .shouldContain("Shared string table stats")
            .shouldHaveExitValue(0);

        ProcessBuilder runPb = ProcessTools.createJavaProcessBuilder(true,
          TestCommon.makeCommandLineForAppCDS(
            "-XX:+UseAppCDS",
            "-XX:+UseCompressedOops",
            "-XX:+UseG1GC",
            "-cp", appJar,
            "-XX:SharedArchiveFile=./SharedStringsBasic.jsa",
            "-Xshare:auto",
            "-showversion",
            "HelloString"));

        TestCommon.executeAndLog(runPb, "run").shouldHaveExitValue(0);
    }
}
