package org.superbiz.moviefun.albums;

import com.amazonaws.util.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        String contentType = uploadedFile.getContentType();
        InputStream inputStream = uploadedFile.getInputStream();

        blobStore.put(new Blob(String.valueOf(albumId), inputStream, contentType));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException {

        Optional<Blob> blob = blobStore.get(String.valueOf(albumId));

        if (blob.isPresent()) {
            byte[] bytes = IOUtils.toByteArray(blob.get().getInputStream());
            HttpHeaders headers = createImageHttpHeaders(blob.get().getContentType(), bytes.length);

            return new HttpEntity<>(bytes, headers);
        }
        return new HttpEntity<>(new byte[]{}, HttpHeaders.EMPTY);
    }


    private HttpHeaders createImageHttpHeaders(String contentType, long length) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(length);
        return headers;
    }

}
