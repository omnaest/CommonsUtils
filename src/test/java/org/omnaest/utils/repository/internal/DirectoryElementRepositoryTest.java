package org.omnaest.utils.repository.internal;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.omnaest.utils.repository.internal.DirectoryElementRepository;

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
        assertEquals("C:\\Temp\\000\\0000000\\00000000000\\000000000000000\\10.json", repository.determineFile(10)
                                                                                                .getCanonicalPath());
        assertEquals("C:\\Temp\\000\\0000000\\00000000000\\000000000000001\\10001.json", repository.determineFile(10001)
                                                                                                   .getCanonicalPath());
        assertEquals("C:\\Temp\\000\\0000000\\00000000001\\000000000010000\\100000001.json", repository.determineFile(100000001)
                                                                                                       .getCanonicalPath());
    }

}
