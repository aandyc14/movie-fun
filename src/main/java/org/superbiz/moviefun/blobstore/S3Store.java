package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class S3Store implements BlobStore {
    private AmazonS3Client s3Client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
        createBucket(photoStorageBucket);
    }

    private void createBucket(String photoStorageBucket) {
        if (!s3Client.doesBucketExist(photoStorageBucket)) {
            s3Client.createBucket(photoStorageBucket);
        }
        System.out.println("####################: Bucket created");
    }

    @Override
    public void put(Blob blob) throws IOException {
        System.out.println("####################: Put");
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);

        byte[] bytes = IOUtils.toByteArray(blob.getInputStream());
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        //metadata.setContentLength(blob.getInputStream().available());
        s3Client.putObject(photoStorageBucket, blob.getName() ,byteArrayInputStream, metadata);
        System.out.println("####################: objectName " + blob.getName() +"content "+blob.getContentType()+ "input stream length");
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        System.out.println("####################: Get");
        if (s3Client.doesObjectExist(photoStorageBucket, name)) {
            S3Object object = s3Client.getObject(photoStorageBucket, name);

            return Optional.of(new Blob(object.getKey(), object.getObjectContent(), object.getObjectMetadata().getContentType()));
        }
        return Optional.empty();
    }

    @Override
    public void deleteAll() {

    }
}
