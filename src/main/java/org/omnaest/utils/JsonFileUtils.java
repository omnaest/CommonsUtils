package org.omnaest.utils;

import java.io.File;

import org.omnaest.utils.functional.Accessor;

/**
 * Helper combining {@link FileUtils} and {@link JSONHelper} functionality
 * 
 * @author omnaest
 */
public class JsonFileUtils
{
    private JsonFileUtils()
    {
        super();
    }

    /**
     * Returns an {@link Accessor} to an json file using the given type for serialization and deserialization
     * 
     * @see FileUtils#toAccessor(File)
     * @see JSONHelper#serializer()
     * @see JSONHelper#deserializer(Class)
     * @param file
     * @param type
     * @return
     */
    public static <E> Accessor<E> toJsonAccessor(File file, Class<E> type)
    {
        return FileUtils.toAccessor(file)
                        .with(JSONHelper.serializer(), JSONHelper.deserializer(type));
    }
}
