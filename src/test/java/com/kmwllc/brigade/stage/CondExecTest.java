package com.kmwllc.brigade.stage;

import com.kmwllc.brigade.concurrency.DumpDocReader;
import com.kmwllc.brigade.document.Document;
import com.kmwllc.brigade.util.BrigadeHelper;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by matt on 3/22/17.
 */
public class CondExecTest {

    @Rule
    public final BrigadeHelper brigadeHelper = new BrigadeHelper("conf/condExec1.properties",
            "conf/condExecConnector.json", "conf/condExecWorkflow.json");
    @Rule
    public final BrigadeHelper brigadeHelper2 = new BrigadeHelper("conf/condExec2.properties",
            "conf/condExecConnector.json", "conf/condExecWorkflow.json");

    @Rule
    public final BrigadeHelper brigadeHelper3 = new BrigadeHelper("conf/condExec1.properties",
            "conf/condExecConnector.json", "conf/condExecWorkflow2.json");

    @Rule
    public final BrigadeHelper brigadeHelper4 = new BrigadeHelper("conf/condExec1.properties",
            "conf/condExecConnector2.json", "conf/condExecWorkflow2.json");
    private File testFile = new File("cond-exec-output.txt");

    @After
    public void cleanup() {
        testFile.delete();
    }



    @Test
    public void testProps1() {
        try {
            brigadeHelper.exec();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Document> docs = new DumpDocReader().read(testFile);
        assertEquals(2, docs.size());
        assertEquals("Blaze", docs.get(0).getField("name").get(0));
        assertEquals("Sonny", docs.get(1).getField("name").get(0));
    }

    @Test
    public void testProps2() {
        try {
            brigadeHelper2.exec();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Document> docs = new DumpDocReader().read(testFile);
        assertEquals(2, docs.size());
        assertEquals("BLAZE", docs.get(0).getField("name").get(0));
        assertEquals("SONNY", docs.get(1).getField("name").get(0));
    }

    @Test
    public void testProps3() {
        System.setProperty("skip", "false");
        try {
            BrigadeHelper bh = new BrigadeHelper("conf/condExec1.properties",
                    "conf/condExecConnector.json", "conf/condExecWorkflow.json");
            bh.exec();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Document> docs = new DumpDocReader().read(testFile);
        assertEquals(2, docs.size());
        assertEquals("BLAZE", docs.get(0).getField("name").get(0));
        assertEquals("SONNY", docs.get(1).getField("name").get(0));
        System.clearProperty("skip");
    }

    @Test
    public void testEnabled1() {
        try {
            brigadeHelper3.exec();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Document> docs = new DumpDocReader().read(testFile);
        assertEquals(2, docs.size());
        assertEquals("BLAZE", docs.get(0).getField("name").get(0));
        assertEquals("SONNY", docs.get(1).getField("name").get(0));
    }

    @Test
    public void testEnabled2() {
        System.setProperty("imShouting", "false");
        try {
            BrigadeHelper bh3 = new BrigadeHelper("conf/condExec1.properties",
                    "conf/condExecConnector.json", "conf/condExecWorkflow2.json");
            bh3.exec();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Document> docs = new DumpDocReader().read(testFile);
        assertEquals(2, docs.size());
        assertEquals("Blaze", docs.get(0).getField("name").get(0));
        assertEquals("Sonny", docs.get(1).getField("name").get(0));
        System.clearProperty("imShouting");
    }

    @Test
    public void testSkipIfField() {
        try {
            brigadeHelper4.exec();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        List<Document> docs = new DumpDocReader().read(testFile);
        assertEquals(2, docs.size());
        assertEquals("Blaze", docs.get(0).getField("name").get(0));
        assertEquals("SONNY", docs.get(1).getField("name").get(0));
    }
}
