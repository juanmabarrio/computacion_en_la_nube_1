package s3.controller;

import com.amazonaws.Response;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        List<S3ObjectSummary> objects = getS3ObjectSummaries(bucketName);
        return new ResponseEntity<>(objects, HttpStatus.OK);
    }

    private List<S3ObjectSummary> getS3ObjectSummaries(@PathVariable String bucketName) {
        ListObjectsV2Result result = s3.listObjectsV2(bucketName);
        return result.getObjectSummaries();
    }

    @PostMapping("/{bucketName}")
    public ResponseEntity<Bucket> createBucket(@PathVariable String bucketName) {
        if (s3.doesBucketExistV2(bucketName))
            return new ResponseEntity("Bucket already exists", HttpStatus.CONFLICT);

        Bucket newBucket = s3.createBucket(bucketName);
        return new ResponseEntity<>(newBucket, HttpStatus.OK);
    }

    @PostMapping("/{bucketName}/uploadObject")
    public ResponseEntity uploadObject(@PathVariable String bucketName, @RequestParam MultipartFile file, @RequestParam Boolean isPublic) {
        try {
            if (!s3.doesBucketExistV2(bucketName))
                return new ResponseEntity("Bucket not found!",HttpStatus.NOT_FOUND);
            PutObjectRequest por = new PutObjectRequest(bucketName,file.getOriginalFilename(),convertMultiPartToFile(file));
            if (isPublic) {
                por.setCannedAcl(CannedAccessControlList.PublicRead);
            }
            s3.putObject(por);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{bucketName}")
    public ResponseEntity deleteBucket(@PathVariable String bucketName) {
        if (!s3.doesBucketExistV2(bucketName))
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        s3.deleteBucket(bucketName);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{bucketName}/{objectName}")
    public ResponseEntity deleteObject(@PathVariable String bucketName, @PathVariable String objectName) {
        if (!(s3.doesObjectExist(bucketName,objectName)))
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        s3.deleteObject(bucketName, objectName);
        return new ResponseEntity(HttpStatus.OK);
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
