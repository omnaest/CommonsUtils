package org.omnaest.utils.repository;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class DirectoryElementRepositoryTest
{

    @Test
    public void testDetermineFile() throws Exception
    {
        DirectoryElementRepository<String> repository = new DirectoryElementRepository<String>(new File("C:/Temp"), String.class)
        {

            @Override
            public File determineFile(long fileIndex)
            {
                return super.determineFile(fileIndex);
            }

        };
        assertEquals("C:\\Temp\\0\\0\\0\\0\\10.json", repository.determineFile(10)
                                                                .getCanonicalPath());
        assertEquals("C:\\Temp\\0\\0\\0\\1\\10001.json", repository.determineFile(10001)
                                                                   .getCanonicalPath());
        assertEquals("C:\\Temp\\0\\0\\1\\10000\\100000001.json", repository.determineFile(100000001)
                                                                           .getCanonicalPath());
    }

}
