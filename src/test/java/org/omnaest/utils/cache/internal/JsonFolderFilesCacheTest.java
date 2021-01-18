package org.omnaest.utils.cache.internal;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache.DataRoot;

public class JsonFolderFilesCacheTest
{
    @Test
    public void testDataRootSerialization()
    {
        DataRoot dataRoot = new JsonFolderFilesCache.DataRoot();
        dataRoot.getData()
                .put("key1", dataRoot.getNextIndex());
        dataRoot.getTypes()
                .put("key1", byte[].class);
        dataRoot.getData()
                .put("key2", dataRoot.getNextIndex());
        dataRoot.getTypes()
                .put("key2", byte[].class);
        dataRoot.getData()
                .put("key1", dataRoot.getNextIndex());
        dataRoot.getTypes()
                .put("key1", byte[].class);
        StringWriter stringWriter = new StringWriter();
        JSONHelper.prepareAsPrettyPrintWriterConsumer(dataRoot)
                  .accept(stringWriter);
        String json = stringWriter.toString();
        assertEquals(2, StringUtils.countMatches(json, "key1"));
        assertEquals(2, StringUtils.countMatches(json, "key2"));

        DataRoot clonedDataRoot = JSONHelper.readerDeserializer(DataRoot.class)
                                            .apply(new StringReader(json));
        assertEquals(2, clonedDataRoot.getData()
                                      .size());
        assertEquals(2, clonedDataRoot.getTypes()
                                      .size());
        assertEquals(Long.valueOf(3l), clonedDataRoot.getData()
                                                     .get("key1"));
        assertEquals(Long.valueOf(2l), clonedDataRoot.getData()
                                                     .get("key2"));
    }
}
