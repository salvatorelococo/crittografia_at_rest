package it.eng.unipa.filesharing.service;

import java.util.List;
import java.util.UUID;

import it.eng.unipa.filesharing.dto.BucketDTO;
import it.eng.unipa.filesharing.dto.BucketTypeDTO;
import it.eng.unipa.filesharing.dto.MembershipDTO;
import it.eng.unipa.filesharing.dto.ResourceDTO;
import it.eng.unipa.filesharing.dto.TeamDTO;

public interface TeamService {
	
	List<TeamDTO> myTeams();

	UUID save(TeamDTO team);
	
	TeamDTO get(UUID uuid);

	void delete(UUID uuid, Boolean recursive);
	
	void acceptInvite(UUID uuid);

	void rejectInvite(UUID uuid);

	void inviteMember(UUID uuid, String otherEmail, boolean admin);
	
	void addMembership(UUID uuid,String bucketName,MembershipDTO membershipDTO);
	
	void removeMember(UUID uuid, String otherEmail);
	
	void addBucket(UUID uuid,BucketDTO bucketDTO);
	
	void removeBucket(UUID uuid, String bucketName);

	List<BucketTypeDTO> bucketTypeSupport();

	ResourceDTO tree(UUID uuid, String name);

	ResourceDTO addCryptedContent(UUID uuid, String bucketName,String parentUniqueId,String name,byte[] content, String hash, byte[] salt, byte[] iv );

	ResourceDTO addContent(UUID uuid, String bucketName, String parentUniqueId, String name, byte[] content);

	ResourceDTO addFolder(UUID uuid, String bucketName, String parentUniqueId, String name);

	ResourceDTO getContent(UUID uuid, String bucketName, String uniqueId);
	
	
}
