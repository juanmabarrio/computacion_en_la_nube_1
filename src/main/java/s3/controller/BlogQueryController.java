package s3.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class BlogQueryController {
//
//	@Autowired
//	private BlogQueryService blogService;
//
//	@GetMapping("/")
//	public List<BasicPostDto> listPosts() {
//		return blogService.getPostsList();
//	}
//
//	@GetMapping("/{id}")
//	public ResponseEntity<FullPostDto> getPost(@PathVariable long id) {
//		FullPostDto post = this.blogService.getPost(id);
//		if (post == null) {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
//		return new ResponseEntity<>(post, HttpStatus.OK);
//	}

}
