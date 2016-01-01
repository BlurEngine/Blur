/*
 * Copyright 2016 Ali Moghnieh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blurengine.blur.modules.maploading;

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.maploading.MapLoaderModule.Archive;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LocalArchiver {

    public static final File DEFAULT_ARCHIVES_FILE = new File("./blur-archives/");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd-HH-mm-ss");

    private final MapLoaderModule mapModule;
    private final File directory;
    private final ArchiveCompressionType compressionType;
    private final Compressor compressor;

    private final String nameTemplate;

    public LocalArchiver(MapLoaderModule mapModule, Archive archive) {
        this.mapModule = mapModule;
        this.compressionType = archive.getCompressionTypeEnum();

        String nameTemplate = archive.getNameTemplate();
        if (nameTemplate == null) {
            nameTemplate = "{mapname}-{datetime}";
        }
        this.nameTemplate = nameTemplate;

        if (archive.getDirectory() == null) {
            this.directory = DEFAULT_ARCHIVES_FILE;
        } else {
            this.directory = new File(archive.getDirectory());
            if (!this.directory.exists()) {
                this.directory.mkdirs();
            }
            Preconditions.checkArgument(directory.isDirectory(), "%s must be a directory.", directory);
        }

        Compressor compressor;

        switch (this.compressionType) {
            case ZIP:
                compressor = new ZipCompress();
                break;
            default:
                compressor = new NoCompress();
        }
        this.compressor = compressor;
    }

    public boolean archive(File file) {
        String archiveName = nameTemplate
            .replaceAll("\\{mapname\\}", file.getName())
            .replaceAll("\\{datetime\\}", dtf.format(LocalDateTime.now()).replaceAll("[+-]+", "-")); // remove + and : in time

        // Precaution against fools who delete the archives directory after load.
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return compressor.compress(file, archiveName);
    }

    private File createArchiveFile(String name) {
        return new File(this.directory, name);
    }

    public Logger getLogger() {
        return mapModule.getLogger();
    }

    private interface Compressor {

        boolean compress(File file, String outputFileName);
    }

    private final class NoCompress implements Compressor {

        @Override
        public boolean compress(File file, String outputFileName) {
            File outputFile = createArchiveFile(outputFileName);
            try {
                FileUtils.copyDirectory(file, outputFile);
                return true;
            } catch (IOException e) {
                mapModule.getLogger().log(Level.SEVERE, "Failed to copy " + file.getPath() + " to " + outputFile.getPath(), e);
                return false;
            }
        }
    }

    private final class ZipCompress implements Compressor {

        // Hack to see if the file has an extension. If not, suffix with .zip
        private boolean suffix = !nameTemplate.matches("\\.+");

        @Override
        public boolean compress(File file, String outputFileName) {
            if (suffix) {
                outputFileName += ".zip";
            }
            File outputFile = createArchiveFile(outputFileName);

            getLogger().fine("Zip compressing... " + file);
            try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
                Files.walk(file.toPath()).filter(Files::isRegularFile).forEach(curPath -> {
                    String path = curPath.toString().replaceFirst("\\.\\\\", ""); // remove .\
                    getLogger().finest("Adding " + path + " to " + outputFile);
                    try {
                        zos.putNextEntry(new ZipEntry(path));
                        zos.write(Files.readAllBytes(curPath));
                        zos.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                getLogger().fine("Done compressing " + file);
                return true;
            } catch (FileNotFoundException e) {
                getLogger().log(Level.SEVERE, outputFileName + " is a directory.", e);
            } catch (SecurityException e) {
                getLogger().log(Level.SEVERE, "No write access to file " + outputFileName, e);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
