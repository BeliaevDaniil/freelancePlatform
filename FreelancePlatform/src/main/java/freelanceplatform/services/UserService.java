package freelanceplatform.services;

import freelanceplatform.data.ProposalRepository;
import freelanceplatform.data.ResumeRepository;
import freelanceplatform.data.UserRepository;
import freelanceplatform.dto.Mapper;
import freelanceplatform.exceptions.NotFoundException;
import freelanceplatform.exceptions.ValidationException;
import freelanceplatform.kafka.ChangesProducer;
import freelanceplatform.kafka.topics.UserChangesTopic;
import freelanceplatform.model.Proposal;
import freelanceplatform.model.Resume;
import freelanceplatform.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static freelanceplatform.kafka.topics.UserChangesTopic.*;

/**
 * The User service
 */
@Service
@CacheConfig(cacheNames={"users"})
@Slf4j
public class UserService implements IService<User, Integer> {
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ProposalRepository proposalRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChangesProducer<UserChangesTopic> userChangesProducer;
    private final Mapper mapper;

    @Autowired
    public UserService(UserRepository userRepository, ResumeRepository resumeRepository, ProposalRepository proposalRepository,
                       PasswordEncoder passwordEncoder, ChangesProducer<UserChangesTopic> userChangesProducer, @Lazy Mapper mapper) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.proposalRepository = proposalRepository;
        this.passwordEncoder = passwordEncoder;
        this.userChangesProducer = userChangesProducer;
        this.mapper = mapper;
    }

    /**
     * Returns User by id
     * @param id user's id
     * @return user
     */
    @Transactional
    @Cacheable
    public Optional<User> findById(Integer id) {
        Objects.requireNonNull(id);
        log.info("Finding user by id {}", id);
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) throw new NotFoundException("User with id " + id + " not found");
        return userOptional;
    }

    /**
     * Return User by username
     * @param username user's username
     * @return user
     */
    @Transactional
    public User findByUsername(String username) {
        Objects.requireNonNull(username);
        log.info("Finding user by username {}", username);
        Optional<User> userOptional = userRepository.getByUsername(username);
        if (userOptional.isEmpty()) throw new NotFoundException("User with username " + username + " not found");
        return userOptional.get();
    }

    /**
     * Find a freelancer by proposal ID.
     * @param  proposalId   the ID of the proposal to search
     * @return              the freelancer associated with the proposal
     */
    @Transactional
    public User findFreelancerByProposalId(Integer proposalId) {
        Objects.requireNonNull(proposalId);
        log.info("Finding freelancer by proposal id {}", proposalId);
        final Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal with id " + proposalId + " not found"));
        return proposal.getFreelancer();
    }

    /**
     * Returns all users
     * @return all users
     */
    @Transactional
    public List<User> findAll() {
        log.info("Finding all users");
        return userRepository.findAll();
    }

    /**
     * Saves the user
     * @param user to save
     */
    @Transactional
    @CachePut(key = "#user.id")
    public User save(User user){
        Objects.requireNonNull(user);
        log.info("Saving user with id {}", user.getId());
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ValidationException("User with this username is already exist");
        }
        if (userRepository.existsByEmail(user.getEmail())){
            throw new ValidationException("User with this email is already exist");
        }
        user.encodePassword(passwordEncoder);
        userRepository.save(user);
        userChangesProducer.sendMessage(mapper.convertUserToJson(user), UserCreated);

        return user;
    }

    /**
     * Updates the user
     * @param user the user data to update
     * @return updated user
     */
    @Transactional
    @CachePut(key = "#user.id")
    public User update(User user){
        Objects.requireNonNull(user);
        log.info("Updating user with id {}", user.getId());
        if (exists(user.getId())) {
            user.encodePassword(passwordEncoder);
            System.out.println(user);
            userChangesProducer.sendMessage(mapper.convertUserToJson(user), UserUpdated);
            return userRepository.save(user);
        } else {
            throw new NotFoundException("User with id " + user.getId() + " not found");
        }
    }

    /**
     * Deletes user
     * @param id - user's id
     */
    @Transactional
    @CacheEvict
    public boolean deleteById(Integer id){
        Objects.requireNonNull(id);
        log.info("Deleting user with id {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    userChangesProducer.sendMessage(mapper.convertUserToJson(user), UserDeleted);
                    return true;
                }).orElse(false);
    }

    /**
     * Checks if user exists
     * @param id user's id
     * @return true if exists / false if not
     */
    public boolean exists(Integer id){
        Objects.requireNonNull(id);
        log.info("Checking if user with id {} exists", id);
        return userRepository.existsById(id);
    }

    /**
     * Saves user's resume
     * @param filename resume name
     * @param content resume content
     * @param user user to add this resume
     */
    @Transactional
    public void saveResume(String filename, byte[] content, User user) {
        Objects.requireNonNull(filename);
        Objects.requireNonNull(content);
        Objects.requireNonNull(user);
        log.info("Saving resume for user with id {}", user.getId());
        if (content.length == 0 || filename.equals("")) throw new ValidationException("Bad inputs");
        Resume resume = new Resume();
        resume.setFilename(filename);
        resume.setContent(content);
        resume.setUser(user);
        resumeRepository.save(resume);
    }

    /**
     * Returns user's resume
     * @param user whose resume to find
     * @return resume
     */
    @Transactional
    public Resume getUsersResume(User user) {
        Objects.requireNonNull(user);
        log.info("Retrieving user resume for user with id {}", user.getId());
        Optional<Resume> resume = resumeRepository.findByUserId(user.getId());
        if (resume.isEmpty()) throw new NotFoundException("Resume for user with id " + user.getId() + " not found");
        return resume.get();
    }
}
