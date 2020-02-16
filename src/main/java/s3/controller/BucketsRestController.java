package s3.controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buckets")
public class BucketsRestController {
    private AmazonS3 s3 = AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.US_EAST_1)
            .build();

    @GetMapping("/")
    public ResponseEntity<List<String>> getAllBuckets() {
        List<String> bucketNames = s3.listBuckets().stream().map(Bucket::getName).collect(Collectors.toList());
        if (bucketNames == null || bucketNames.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bucketNames, HttpStatus.OK);
    }

    @GetMapping("/{bucketName}")
    public ResponseEntity<Bucket> getBucket(@PathVariable String bucketName) {
        Bucket bucket = s3.listBuckets().stream()
                .filter(b -> b.getName().equals(bucketName))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
        return new ResponseEntity<>(bucket, HttpStatus.OK);
    }

    @GetMapping("/{bucketName}/objects")
    public ResponseEntity<List<S3ObjectSummary>> getObjects(@PathVariable String bucketName) {
        //List<String> bucketNames =
//        Bucket bucket = s3.listBuckets().stream()
//                .filter(b -> b.getName().equals(bucketName))
//                .findFirst()
//                .orElseThrow(EntityNotFoundException::new);
        ListObjectsV2Result result = s3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        return new ResponseEntity<>(objects, HttpStatus.OK);
    }

    @PostMapping("/{bucketName}")
    public ResponseEntity<Bucket> createBucket(@PathVariable String bucketName) {
        if (s3.doesBucketExistV2(bucketName))
            throw new EntityExistsException();
        Bucket newBucket = s3.createBucket(bucketName);
        return new ResponseEntity<>(newBucket, HttpStatus.OK);
    }

    @PostMapping("/{bucketName}/uploadObject")
    public ResponseEntity uploadObject(@PathVariable String bucketName, @RequestBody String fileContent) {
        if (!s3.doesBucketExistV2(bucketName))
            throw new EntityNotFoundException();
        Bucket newBucket = s3.createBucket(bucketName);
        PutObjectRequest por = new PutObjectRequest(
                bucketName,
                createRandomFileName(),
                fileContent
        );
        por.setCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(por);
        return new ResponseEntity(HttpStatus.OK);
    }


    private String createRandomFileName() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
