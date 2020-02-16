package s3.controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;
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
        if ( bucketNames == null || bucketNames.size()==0 ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bucketNames, HttpStatus.OK);
    }

    @GetMapping("/{bucket_name}")
    public ResponseEntity<String> getBucket(@PathVariable long bucketName) {
        //List<String> bucketNames =
        Bucket bucket = s3.listBuckets().stream()
                .filter(b -> b.getName().equals(bucketName))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
        return new ResponseEntity<>(bucket.toString(), HttpStatus.OK);
    }





}
