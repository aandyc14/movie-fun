package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        String coverFileName = format("covers/%s", blob.getName());
        File targetFile = new File(coverFileName);

        Files.copy(blob.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        IOUtils.closeQuietly(blob.getInputStream());
    }


    @Override
    public Optional<Blob> get(String name) throws IOException {
        try{
            Path coverFilePath = getExistingCoverPath(Long.parseLong(name));
            byte[] imageBytes = Files.readAllBytes(coverFilePath);
            InputStream inputStream = Files.newInputStream(coverFilePath);

            String contentType = new Tika().detect(coverFilePath);
            String coverFileName = format("covers/%s", name);

            return Optional.of(new Blob(coverFileName, inputStream,contentType));
        }
        catch(URISyntaxException ex){

        }
        return null;
    }


    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

    @Override
    public void deleteAll() {
        // ...
    }
}
