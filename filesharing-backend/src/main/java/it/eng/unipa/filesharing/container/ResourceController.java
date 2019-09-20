package it.eng.unipa.filesharing.container;

import java.io.*;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

import it.eng.unipa.filesharing.dto.PasswordDTO;
import org.assertj.core.util.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import it.eng.unipa.filesharing.dto.FolderDTO;
import it.eng.unipa.filesharing.dto.ResourceDTO;
import it.eng.unipa.filesharing.service.TeamService;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

@RestController
@RequestMapping("/resource")
public class ResourceController {
	
	@Autowired
	TeamService teamService;
	
	@GetMapping("/{uuid}/{bucketName}")
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceDTO tree(@PathVariable("uuid") UUID uuid,@PathVariable("bucketName") String bucketName){
		return teamService.tree(uuid,bucketName);
	}
	
	
	@PostMapping("/addFolder/{uuid}/{bucketName}")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void addFolder(@PathVariable("uuid") UUID uuid,@PathVariable("bucketName") String bucketName,@RequestBody FolderDTO folderDTO) {
		teamService.addFolder(uuid, bucketName, folderDTO.getParentUniqueId(), folderDTO.getName());
	}

//	public static File convert(MultipartFile file) throws IOException {
//		File convFile = new File(file.getOriginalFilename());
//		convFile.createNewFile();
//		FileOutputStream fos = new FileOutputStream(convFile);
//		fos.write(file.getBytes());
//		fos.close();
//		return convFile;
//	}

	@PostMapping("/addContent/{uuid}/{bucketName}")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void addContent(@PathVariable("uuid") UUID uuid,@PathVariable("bucketName") String bucketName,@RequestParam(value = "parentUniqueId", required = false)String parentUniqueId,@RequestParam("file") MultipartFile multipartFile, @RequestParam(value = "password", required = false) String password) throws IOException{
		if(password == null){
			teamService.addContent(uuid, bucketName, parentUniqueId, multipartFile.getOriginalFilename(), multipartFile.getBytes());
		}else{
			teamService.addCryptedContent(uuid, bucketName, parentUniqueId, multipartFile.getOriginalFilename(), multipartFile.getBytes(), password);
		}

	}

	/*
	@PostMapping("/addContent/{uuid}/{bucketName}")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void addContent(@PathVariable("uuid") UUID uuid,@PathVariable("bucketName") String bucketName,@RequestParam(value = "parentUniqueId", required = false)String parentUniqueId,@RequestParam("file") MultipartFile multipartFile) throws IOException {
		teamService.addContent(uuid, bucketName, parentUniqueId, multipartFile.getOriginalFilename(), multipartFile.getBytes());
	}
	 */

	@PostMapping("/{uuid}/{bucketName}/{uniqueId}")
	public ResponseEntity<Resource> download(@PathVariable("uuid") UUID uuid,@PathVariable("bucketName") String bucketName,@PathVariable("uniqueId") String uniqueId, @RequestBody PasswordDTO passwordDTO) {
		if(passwordDTO.getPassword() == null) {
			ResourceDTO resourceDTO = teamService.getContent(uuid, bucketName, uniqueId);
			return getResponseEntityResource(resourceDTO.getName(), resourceDTO.getContent());
		}
		else {
			ResourceDTO resourceDTO = teamService.getCryptedContent(uuid, bucketName, uniqueId, passwordDTO.getPassword());
			return getResponseEntityResource(resourceDTO.getName(), resourceDTO.getContent());
		}
	}

	private ResponseEntity<Resource> getResponseEntityResource(String name, byte[] body) {
		return ResponseEntity.ok()
                //.contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .body(new InputStreamResource(new ByteArrayInputStream(body)));
	}
}
