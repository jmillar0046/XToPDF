package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map-based registry that maps file extensions to FileConverter instances.
 * Populated at startup via Spring's auto-discovery of all FileConverter beans.
 *
 * Adding a new converter only requires creating a new @Component class that
 * implements FileConverter — no changes to this registry or the service.
 */
@Component
public class ConverterRegistry {

    private final Map<String, FileConverter> converterMap;

    public ConverterRegistry(List<FileConverter> converters) {
        this.converterMap = new HashMap<>();
        for (FileConverter converter : converters) {
            for (String ext : converter.getSupportedExtensions()) {
                converterMap.put(ext.toLowerCase(), converter);
            }
        }
    }

    /**
     * Returns the FileConverter for the given file extension.
     *
     * @param extension the file extension including the leading dot (e.g. ".png")
     * @return the corresponding FileConverter
     * @throws FileConversionException if no converter is registered for the extension
     */
    public FileConverter getConverter(String extension) throws FileConversionException {
        FileConverter converter = converterMap.get(extension.toLowerCase());
        if (converter == null) {
            throw new FileConversionException("Unsupported file format: " + extension);
        }
        return converter;
    }

    /**
     * Returns an unmodifiable set of all registered file extensions.
     */
    public Set<String> getSupportedExtensions() {
        return Collections.unmodifiableSet(converterMap.keySet());
    }
}
