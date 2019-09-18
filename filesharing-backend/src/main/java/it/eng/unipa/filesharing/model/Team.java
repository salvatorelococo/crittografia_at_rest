package it.eng.unipa.filesharing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.PreDestroy;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostRemove;
import javax.persistence.PreRemove;

import it.eng.unipa.filesharing.context.SecurityContext;
import it.eng.unipa.filesharing.resource.BucketType;
import it.eng.unipa.filesharing.resource.ContentResource;
import it.eng.unipa.filesharing.resource.FolderResource;
import it.eng.unipa.filesharing.service.exception.BucketNotFoundException;
import it.eng.unipa.filesharing.service.exception.UserRoleNotFoundException;

/*@Document(collection = "teams")*/
@Entity
public class Team {

	/*@Id*/
	@Id
	private UUID uuid;
	
	private String name;
	private String description;
	
	@OneToMany(mappedBy = "team",cascade = CascadeType.ALL,orphanRemoval = true)
	private List<Bucket> buckets = new ArrayList<>();
	
	@OneToMany(mappedBy = "team",cascade = CascadeType.ALL,orphanRemoval = true)
	private List<UserRole> members = new ArrayList<>();

	/*public Team(String uuid,User creator,String name,String description) {
		this.uuid = UUID.fromString(uuid);
		this.creator = creator;
		this.name = name;
		this.description = description;
	}*/
	
	public Team() {
		// TODO Auto-generated constructor stub
	}

	public Team(String creator,String name,String description) {
		this.uuid = UUID.randomUUID();
		this.name = name;
		this.description = description;
		this.members.add(new UserRole(this, creator,true,true));
	}

	public List<UserRole> getMembers() {
		return Collections.unmodifiableList(members);
	}

	public List<Bucket> getBuckets() {
		return Collections.unmodifiableList(buckets);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/*public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}*/

	public boolean addBucket(BucketType bucketType,String email,String bucketName,String description) {
		return adminExecute(email, (user)->{
			Optional<Bucket> findFirst = this.buckets.stream().filter((t)->t.getName().equals(bucketName)).findFirst();
			if(!findFirst.isPresent()) {
				Bucket b = new Bucket(this, bucketType, bucketName, description, email);
				this.getMembers().forEach(x->b.addMembership(x.getOid().getEmail(), true, true));

				return this.buckets.add(b);
			}else {
				return false;
			}
			//return this.buckets.add(new Bucket(this,bucketName, description,user));
		});
	}

	public boolean removeBucket(String email,String bucketName) {
		return adminExecute(email, (u)->{
			Optional<Bucket> findFirst = this.buckets.stream().filter((t)->t.getName().equals(bucketName)).findFirst();
			if(findFirst.isPresent()) {
				return this.buckets.remove(findFirst.get());
			}else {
				return false;
			}
		});
	}

	public boolean inviteMember(String email,String otherEmail,boolean admin) {
		return adminExecute(email, (u)->{
			UserRole userRole = new UserRole(this,otherEmail, admin);
			if(!this.members.contains(userRole)) {
				return this.members.add(userRole);
			}else {
				return false;
			}
		});
	}
	
	
	public boolean acceptInvite(String email,String longName) {
		Optional<UserRole> findFirst = this.members.stream().filter((t)->t.getOid().getEmail().equals(email)).findFirst();
		if(findFirst.isPresent()) {
			findFirst.get().active(longName);
			return true;
		}else {
			return false;
		}
	}

	public boolean rejectInvite(String email) {
		Optional<UserRole> findFirst = this.members.stream().filter((t)->t.getOid().getEmail().equals(email)).findFirst();
		if(findFirst.isPresent() && !findFirst.get().isActive()) {
			return this.members.remove(findFirst.get());
		}else {
			return false;
		}
	}

	public boolean removeMember(String email,String otherEmail) {
		return adminExecute(email, (u)->{
			UserRole o = new UserRole(this,otherEmail,false);

			/*int indexOf = this.members.indexOf(o);
			if(indexOf>=0) {
				o = this.members.get(indexOf);
				boolean remove = this.members.remove(o);
				o.clearTeam();
				return remove;
			}else {
				return false;
			}*/


			if(this.members.contains(o)) {

				this.getBuckets().forEach((bucket)->{
					bucket.removeMembership(otherEmail);
				});


				return this.members.remove(o);
			}else {
				return false;
			}

		});
	}

	public boolean addMembership(String email,String bucketName,String otherEmail,boolean create, boolean delete) {
		return adminExecute(email, (userAdmin)->{
			Bucket bucket = bucket(bucketName);
			UserRole userRole = userRole(otherEmail);//solleva eccezione se non presente
			return bucket.addMembership(otherEmail, create, delete);
			

		});

	}


	private <R> R adminExecute(String email,Function<UserRole,R> c) {
		UserRole userRole = userRole(email);
		if(userRole.isAdmin() && userRole.isActive()) {
			return c.apply(userRole);
		}else {
			throw new RuntimeException("L'utente "+email+" non e' un membro del team "+this.getName()+" o non e' un amministratore o non e' attivo");
		}
	}
	
	private <R> R userExecute(String email,Function<UserRole,R> c) {
		UserRole userRole = userRole(email);
		if(userRole.isAdmin() && userRole.isActive()) {
			return c.apply(userRole);
		}else {
			throw new RuntimeException("L'utente "+email+" non e' un membro del team "+this.getName()+" o non e' un amministratore o non e' attivo");
		}
	}

	private UserRole userRole(String email) {
		UserRole userRole = getMembers().stream().filter((u)->u.getOid().getEmail().equals(email)).findFirst().orElseThrow(()->new UserRoleNotFoundException(email));
		return userRole;
	}

	
	public Bucket bucket(String bucketName) {
		return getBuckets().stream().filter((u)->u.getName().equals(bucketName)).findFirst().orElseThrow(()->new BucketNotFoundException(this.getUuid(), bucketName));
	}
	
	public ContentResource addCryptedContent(String bucketName,String parentUniqueId,String email,String name,byte[] content) {
		Bucket bucket = bucket(bucketName);
		return bucket.addCryptedContent(email, parentUniqueId, name, content);
	}

	public ContentResource addContent(String bucketName,String parentUniqueId,String email,String name,byte[] content) {
		Bucket bucket = bucket(bucketName);
		return bucket.addContent(email, parentUniqueId, name, content);
	}
	
	public FolderResource addFolder(String bucketName,String parentUniqueId,String email,String name) {
		Bucket bucket = bucket(bucketName);
		return bucket.addFolder(email, parentUniqueId, name);
	}

	public ContentResource getContent(String email,String bucketName, String uniqueId) {
		return userExecute(email, (userRole)->{
			Bucket bucket = bucket(bucketName);
			return bucket.getContent(uniqueId);
		});
	}
}
