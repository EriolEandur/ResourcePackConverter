package net.hypixel.resourcepack.impl;

import net.hypixel.resourcepack.Converter;
import net.hypixel.resourcepack.PackConverter;
import net.hypixel.resourcepack.pack.Pack;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class ManualUpdateConverter extends Converter {
    
    private final String MANUAL_UPDATE_SUFFIX = "_manual_updates";

    public ManualUpdateConverter(PackConverter packConverter) {
        super(packConverter);
    }

    @Override
    public void convert(Pack pack) throws IOException {
        final Path manualUpdates = pack.getOriginalPath().getParent().resolve(pack.getFileName()+MANUAL_UPDATE_SUFFIX);
        final Path target = pack.getWorkingPath();
        Files.walkFileTree(manualUpdates, EnumSet.noneOf(FileVisitOption.class)/*(FileVisitOption.FOLLOW_LINKS)*/, Integer.MAX_VALUE,
            new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
                {
                    Path targetdir = target.resolve(manualUpdates.relativize(dir));
                    try {
                        Files.copy(dir, targetdir,StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (FileAlreadyExistsException e) {
                         if (!Files.isDirectory(targetdir))
                             throw e;
                    }
                    return CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
                {
                    Files.copy(file, target.resolve(manualUpdates.relativize(file)),
                                                  StandardCopyOption.COPY_ATTRIBUTES,
                                                  StandardCopyOption.REPLACE_EXISTING);
                    return CONTINUE;
                }
            });
    }
}
