package it.eng.unipa.filesharing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import it.eng.unipa.filesharing.dto.ResourceDTO.TYPE;
import it.eng.unipa.filesharing.repository.jpaconverter.BucketTypeConverter;
import it.eng.unipa.filesharing.resource.BucketResource;
import it.eng.unipa.filesharing.resource.BucketType;
import it.eng.unipa.filesharing.resource.ContentResource;
import it.eng.unipa.filesharing.resource.FolderResource;
import it.eng.unipa.filesharing.resource.Resource;
import it.eng.unipa.filesharing.resource.ResourceRepository;
import it.eng.unipa.filesharing.service.exception.ContentNotFoundException;

@Entity
@Table(
	    uniqueConstraints=
	        @UniqueConstraint(columnNames={"uuid", "name"})
	)
public class Bucket {
	
	@Id
	@SequenceGenerator(name="bucket_seq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="bucket_seq")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "uuid")
	private Team team;
	
	@Column(name = "name")
	private String name;

	private String description;
	
	@Convert(converter = BucketTypeConverter.class)
	private BucketType bucketType;

	// TODO: Aggiunto
	@OneToMany(mappedBy = "bucket", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<File> files = new ArrayList<File>();


	@OneToMany(mappedBy = "bucket",cascade = CascadeType.ALL,orphanRemoval = true)
	private List<Membership> memberships = new ArrayList<Membership>();
	
	public Bucket() {}
	
	public Bucket(Team team,BucketType bucketType,String name,String description,String email) {
		this.team = team;
		this.bucketType = bucketType;
		this.name = name;
		this.description = description;
		this.memberships.add(new Membership(email, this, true, true));
	}
	
	public String getName() {
		return name;
	}
		
	public String getDescription() {
		return description;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public Long getId() {
		return id;
	}
	
	public List<Membership> getMemberships() {
		return Collections.unmodifiableList(memberships);
	}
	
	public boolean addMembership(String email,boolean create,boolean delete) {
		Optional<Membership> membershipOpt = membership(email);
		if(membershipOpt.isPresent()) {
			Membership membership = membershipOpt.get();
			membership.setPermissionCreate(create);
			membership.setPermissionDelete(delete);
			return true;
		}else {
			return this.memberships.add(new Membership(email, this, create, delete));
		}
	}
	
	
	public BucketType getBucketType() {
		return bucketType;
	}
	
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Bucket)) {
            return false;
        }
        Bucket that = (Bucket) obj;
        EqualsBuilder eb = new EqualsBuilder();
        eb.append(this.getId(), that.getId());
        return eb.isEquals();
    }
	
	@Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(this.getId());
        return hcb.toHashCode();
    }
	
	
	public BucketResource getBucketResource(){
		ResourceRepository repository = bucketType.getRepository();
		return repository.loadBucket(this.getTeam().getUuid(), this.getName());
	}

	// TODO: Aggiunto
    public ContentResource addCryptedContent(String email,String parentUniqueId, String name, byte[] content, String hash, byte[] salt, byte[] iv) {
        return execute(email,(member)->{
            if(member.isPermissionCreate()) {
                ResourceRepository repository = bucketType.getRepository();
                Resource parent = parentUniqueId!=null ? repository.read(getBucketResource(),parentUniqueId) : getBucketResource();

                // TODO: Aggiunto (PROBLEMA: Il file viene in ogni caso aggiunto alla lista(?) e prima di essere creato)
                File f = new File(this, parentUniqueId, name, hash, salt, iv);
                this.files.add(f);

                // TODO: Invocare funzione di criptaggio per il file prima delle creazione del contenuto.
                // content = qualcuno.cryptFile(content, hash, iv);
                return repository.createContent(parent, name, content);
            } else {
                return null;
            }
        });
    }

	public ContentResource addContent(String email,String parentUniqueId,String name,byte[] content) {
		return execute(email,(member)->{
			if(member.isPermissionCreate()) {
				ResourceRepository repository = bucketType.getRepository();
				Resource parent = parentUniqueId!=null ? repository.read(getBucketResource(),parentUniqueId) : getBucketResource();
				return repository.createContent(parent, name, content);
			} else {
				return null;
			}
		});
	}

	public FolderResource addFolder(String email,String parentUniqueId,String name) {
		return execute(email,(member)->{
			if(member.isPermissionCreate()) {
				ResourceRepository repository = bucketType.getRepository();
				Resource parent = parentUniqueId!=null ? repository.read(getBucketResource(),parentUniqueId) : getBucketResource();
				return repository.createFolder(parent, name);
			}else {
				return null;
			}
		});
	}
	
	
	@PostPersist
	public void postPersist() {
		System.out.println("------------------------ post persist");
		ResourceRepository repository = bucketType.getRepository();
		repository.createBucket(getTeam().getUuid(), getName());
	}
	
	
	@PostRemove
	public void postRemove() {
		System.out.println("------------------------ post remove");
		ResourceRepository repository = bucketType.getRepository();
		repository.deleteBucket(getTeam().getUuid(),getName());
	}
	
	
	private <R> R execute(String email,Function<Membership,R> c) {
		Optional<Membership> membershipOpt = membership(email);
		if(membershipOpt.isPresent()) {
			return c.apply(membershipOpt.get());
		}else {
			throw new RuntimeException("L'utente "+email+" non e' un membro del team "+this.getName()+" o non e' un amministratore o non e' attivo");
		}
	}

	private Optional<Membership> membership(String email) {
		Optional<Membership> membershipOpt = getMemberships().stream().filter((u)->u.getEmail().equals(email)).findFirst();
		return membershipOpt;
	}

	public void removeMembership(String otherEmail) {
		Optional<Membership> membershipOpt = membership(otherEmail);
		if(membershipOpt.isPresent()) {
			this.memberships.remove(membershipOpt.get());
		}
	}

	public ContentResource getContent(String uniqueKey) {
		ResourceRepository repository = bucketType.getRepository();
		Resource read = repository.read(getBucketResource(), uniqueKey);
		if(read!=null && read.isType(it.eng.unipa.filesharing.resource.Resource.TYPE.CONTENT)) {
			return (ContentResource)read;
		}else {
			throw new ContentNotFoundException(uniqueKey);
		}
	}
}
