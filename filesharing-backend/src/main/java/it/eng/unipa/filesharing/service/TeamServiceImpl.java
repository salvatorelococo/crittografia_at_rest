package it.eng.unipa.filesharing.service;

import java.io.*;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import it.eng.unipa.filesharing.dto.*;
import it.eng.unipa.filesharing.security.AES;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.eng.unipa.filesharing.context.SecurityContext;
import it.eng.unipa.filesharing.model.Bucket;
import it.eng.unipa.filesharing.model.Team;
import it.eng.unipa.filesharing.repository.TeamRepository;
import it.eng.unipa.filesharing.resource.BucketResource;
import it.eng.unipa.filesharing.resource.BucketType;
import it.eng.unipa.filesharing.resource.ContentResource;
import it.eng.unipa.filesharing.resource.FolderResource;
import it.eng.unipa.filesharing.service.exception.BucketNotFoundException;
import it.eng.unipa.filesharing.service.exception.TeamNotFoundException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TeamServiceImpl implements TeamService{
	
	private TeamRepository teamRepository;
	
	private ConversionService conversionService;
	
	private List<BucketType> allBucketType;
	
	public TeamServiceImpl(/*@Autowired ResourceRepository resourceRepository,*/@Autowired TeamRepository teamRepository,@Autowired ConversionService conversionService,@Autowired List<BucketType> allBucketType) {
		this.teamRepository = teamRepository;
		this.conversionService = conversionService;
		this.allBucketType = allBucketType;
	}
	
	@Override
	public TeamDTO get(UUID uuid) {
		Team team = team(uuid);
		
		return conversionService.convert(team, TeamDTO.class);
		
	}

	@Override
	public List<TeamDTO> myTeams() {
		return teamRepository.myTeams(SecurityContext.getEmail()).stream().map((t)->{
			return conversionService.convert(t, TeamDTO.class);
		}).collect(Collectors.toList());
	}
	
	
	@Override
	public UUID save(TeamDTO teamDTO) {
		Team team = null;
		if(teamDTO.getUuid()!=null) {
			team = team(teamDTO.getUuid());
			team.setName(teamDTO.getName());
			team.setDescription(teamDTO.getDescription());
		}else {
			team = new Team(SecurityContext.getEmail(), teamDTO.getName(), teamDTO.getDescription());
			for(UserRoleDTO x: teamDTO.getMembers()) {
				team.inviteMember(SecurityContext.getEmail(), x.getEmail(), x.isAdmin());
			}
		}
		
		return teamRepository.save(team).getUuid();
	}
	
	@Override
	public void delete(UUID uuid, Boolean recursive) {
		Team team = team(uuid);
		teamRepository.delete(team);
	}
	
	@Override
	public void inviteMember(UUID uuid, String otherEmail,boolean admin) {
		Team team = team(uuid);
		team.inviteMember(SecurityContext.getEmail(), otherEmail, admin);
	}
	
	@Override
	public void acceptInvite(UUID uuid) {
		Team team = team(uuid);
		boolean esito = team.acceptInvite(SecurityContext.getEmail(),SecurityContext.getLongName());
		team.getBuckets().forEach(x-> x.addMembership(SecurityContext.getEmail(), true, true));
		teamRepository.save(team);
	}

	@Override
	public void rejectInvite(UUID uuid) {
		Team team = team(uuid);
		boolean esito = team.rejectInvite(SecurityContext.getEmail());
	}
	
	@Override
	public void removeMember(UUID uuid, String otherEmail) {
		Optional<Team> findById = teamRepository.findById(uuid);
		if(findById.isPresent() ){
			Team team = findById.get();
			boolean esito = team.removeMember(SecurityContext.getEmail(), otherEmail);
		}
	}
	
	@Override
	public void addBucket(UUID uuid,BucketDTO bucketDTO) {
		Optional<Team> findById = teamRepository.findById(uuid);
		if(findById.isPresent() ){
			Team team = findById.get();
			
			BucketType bucketType = contains(bucketDTO.getBucketType());
			
			boolean esito = team.addBucket(bucketType,SecurityContext.getEmail(), bucketDTO.getName(),bucketDTO.getDescription());	
			
		}
		
	}
	
	
	@Override
	public void removeBucket(UUID uuid, String name) {
		Optional<Team> findById = teamRepository.findById(uuid);
		if(findById.isPresent() ){
			Team team = findById.get();
			boolean esito = team.removeBucket(SecurityContext.getEmail(), name);			
		}
		
	}
	
	@Override
	public List<BucketTypeDTO> bucketTypeSupport(){
		return this.allBucketType.stream().map((bt)->new BucketTypeDTO(bt.getName(),bt.getDescription())).collect(Collectors.toList());
	}
	
	private BucketType contains(String bucketTypeName) {
		return this.allBucketType.stream().filter((b)->b.getName().equals(bucketTypeName)).findFirst().orElseGet(null);
	}

	@Override
	public ResourceDTO tree(UUID uuid, String bucketName) {
		Team team = team(uuid);
		Bucket bucket = team.bucket(bucketName);
		return (ResourceDTO)conversionService.convert(bucket.getBucketResource(),TypeDescriptor.valueOf(BucketResource.class), TypeDescriptor.valueOf(ResourceDTO.class));
	}

	private Team team(UUID uuid) {
		Team team = teamRepository.findById(uuid).orElseThrow(() -> new TeamNotFoundException(uuid));
		return team;
	}

	@Override
	public void addMembership(UUID uuid, String bucketName, MembershipDTO membershipDTO) {
		Team team = team(uuid);
		team.addMembership(SecurityContext.getEmail(), bucketName, membershipDTO.getEmail(), membershipDTO.isPermissionCreate(), membershipDTO.isPermissionDelete());
	}

	@Override
	public ResourceDTO addCryptedContent(UUID uuid, String bucketName,String parentUniqueId,String name,byte[] content, String password) {
		Team team = team(uuid);
		byte[] cryptedFileDTO = AES.encrypt(content, password);
		ContentResource contentResource = team.addContent(bucketName, parentUniqueId, SecurityContext.getEmail(), name + ".crypt", cryptedFileDTO);
		return conversionService.convert(contentResource, ResourceDTO.class);
	}

	@Override
	public ResourceDTO addContent(UUID uuid, String bucketName,String parentUniqueId,String name,byte[] content) {
		Team team = team(uuid);
		ContentResource contentResource = team.addContent(bucketName, parentUniqueId, SecurityContext.getEmail(), name, content);
		return conversionService.convert(contentResource, ResourceDTO.class);
	}
	
	@Override
	public ResourceDTO addFolder(UUID uuid, String bucketName,String parentUniqueId,String name) {
		Team team = team(uuid);
		FolderResource folderResource = team.addFolder(bucketName, parentUniqueId, SecurityContext.getEmail(), name);
		return conversionService.convert(folderResource, ResourceDTO.class);
	}


	public ResourceDTO getCryptedContent (UUID uuid, String bucketName, String uniqueId, String password) {
		Team team = team(uuid);
		ContentResource contentResource = team.getContent(SecurityContext.getEmail(),bucketName,uniqueId);
		ResourceDTO resourceDTO = (ResourceDTO)conversionService.convert(contentResource,TypeDescriptor.valueOf(ContentResource.class), TypeDescriptor.valueOf(ResourceDTO.class));
		// Decrypting
		byte[]  decryptedContent = AES.decrypt(contentResource.getContent(), password);
		resourceDTO.setContent(decryptedContent);

		// TODO: I file criptati contenti ".crypt" all'interno del nome vengono scaricati senza di esso nel nome finale.
		String fileNameWithoutCrypt = resourceDTO.getName().replace(".crypt", "");
//		String[] splitted = resourceDTO.getName().split("\\.");
//		String fileNameWithoutCrypt = StringUtils.join(splitted,".");
		resourceDTO.setName(fileNameWithoutCrypt);

		return resourceDTO;
	}
	
	@Override
	public ResourceDTO getContent(UUID uuid, String bucketName, String uniqueId) {
		
		Team team = team(uuid);
		ContentResource contentResource = team.getContent(SecurityContext.getEmail(),bucketName,uniqueId);
		ResourceDTO resourceDTO = (ResourceDTO)conversionService.convert(contentResource,TypeDescriptor.valueOf(ContentResource.class), TypeDescriptor.valueOf(ResourceDTO.class));
		//		byte[]  decryptedContent = AES.decrypt(contentResource.getContent(), password);
		//		resourceDTO.setContent(decryptedContent);
		//		String[] splitted = resourceDTO.getName().split("\\.");
		// 		StringUtils.join(splitted,".")
		// 		resourceDTO.setName(fileNameWithoutCrypt);
		return resourceDTO;
	}

	private CryptedFileDTO cryptFile(byte[] toCrypt, String password){
		try {
		//byte[] myBytes = multipartFile.getBytes();
		//File nomeFile = this.convert(multipartFile);


		//FileInputStream inFile = new FileInputStream(nomeFile);

		InputStream inFile = new ByteArrayInputStream(toCrypt);

		// encrypted file
		ByteArrayOutputStream outFile = new ByteArrayOutputStream();

		// password to encrypt the file
		// passata come parametro
		// password, iv and salt should be transferred to the other end
		// in a secure manner

		// salt is used for encoding
		// writing it to a file
		// salt should be transferred to the recipient securely
		// for decryption


		byte[] salt = new byte[8];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(salt);
//		FileOutputStream saltOutFile = new FileOutputStream("salt.enc");
//		saltOutFile.write(salt);
//		saltOutFile.close();

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536,
				256);
		SecretKey secretKey = factory.generateSecret(keySpec);


		byte[] hash = secretKey.getEncoded();
		// Salva key e algoritmo
		SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

		//
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secret);
		AlgorithmParameters params = cipher.getParameters();

		// iv adds randomness to the text and just makes the mechanism more
		// secure
		// used while initializing the cipher
		// file to store the iv


//		FileOutputStream ivOutFile = new FileOutputStream("iv.enc");

			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

//		ivOutFile.write(iv);
//		ivOutFile.close();

		//file encryption
		byte[] input = new byte[64];
		int bytesRead;

		while ((bytesRead = inFile.read(input)) != -1) {
			byte[] output = cipher.update(input, 0, bytesRead);
			if (output != null)
				outFile.write(output);
		}

		byte[] output = cipher.doFinal();
		if (output != null)
			outFile.write(output);

		inFile.close();
		outFile.flush();
		outFile.close();

		System.out.println("File Encrypted.");
		return new CryptedFileDTO(outFile.toByteArray(), hash, salt, iv);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
