package freelanceplatform.controllers;

import freelanceplatform.dto.Mapper;
import freelanceplatform.dto.entityCreationDTO.ProposalCreationDTO;
import freelanceplatform.dto.entityDTO.ProposalDTO;
import freelanceplatform.model.Proposal;
import freelanceplatform.model.User;
import freelanceplatform.model.security.UserDetails;
import freelanceplatform.services.ProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Controller for managing proposals.
 */
@Slf4j
@RestController
@RequestMapping("/rest/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;
    private final Mapper mapper;

    private final static ResponseEntity<Void> FORBIDDEN1 = new ResponseEntity<>(FORBIDDEN);
    private final static ResponseEntity<Void> BAD_REQUEST = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    /**
     * Finds a proposal by its ID.
     *
     * @param id the ID of the proposal
     * @return the proposal DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProposalDTO> findById(@PathVariable Integer id) {
        return proposalService.findById(id)
                .map(pr -> ResponseEntity.ok(mapper.proposalToProposalDTO(pr)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Finds all proposals.
     *
     * @return a list of all proposal DTOs
     */
    @GetMapping()
    public ResponseEntity<List<ProposalDTO>> findAll() {
        return ResponseEntity.ok(proposalService.findAll().stream()
                .map(mapper::proposalToProposalDTO).toList());
    }

    /**
     * Updates an existing proposal.
     *
     * @param id          the ID of the proposal to update
     * @param proposalDTO the proposal DTO with updated information
     * @param auth        the authentication object
     * @return a response entity indicating the outcome
     */
    @PreAuthorize("hasAnyRole({'ROLE_USER', 'ROLE_ADMIN'})")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody ProposalDTO proposalDTO, Authentication auth) {
        Objects.requireNonNull(proposalDTO);

        if (!proposalDTO.getId().equals(id)) return BAD_REQUEST;
        if (!hasUserAccess(proposalDTO, auth)) return FORBIDDEN1;

        Proposal newPr = mapper.proposalDTOToProposal(proposalDTO);

        proposalService.update(newPr);
        return ResponseEntity.noContent().build();
    }

    /**
     * Saves a new proposal.
     *
     * @param proposalCreationDTO the proposal DTO to save
     * @param auth                the authentication object
     * @return a response entity indicating the outcome
     */
    @PreAuthorize("hasAnyRole({'ROLE_USER', 'ROLE_ADMIN'})")
    @PostMapping()
    public ResponseEntity<Void> save(@RequestBody ProposalCreationDTO proposalCreationDTO, Authentication auth) {
        Objects.requireNonNull(proposalCreationDTO);

        if (!hasUserAccess(proposalCreationDTO, auth)) return FORBIDDEN1;

        Proposal newPr = mapper.proposalCreationDTOToProposal(proposalCreationDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(proposalService.save(newPr).getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * Deletes a proposal by its ID.
     *
     * @param id the ID of the proposal to delete
     * @return a response entity indicating the outcome
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        return proposalService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Checks if the authenticated user has access to the proposal.
     *
     * @param proposalDTO the proposal DTO
     * @param auth        the authentication object
     * @return true if the user has access, false otherwise
     */
    private static Boolean hasUserAccess(ProposalDTO proposalDTO, Authentication auth) {
        User user = ((UserDetails) auth.getPrincipal()).getUser();
        return user.isAdmin() || user.getId().equals(proposalDTO.getFreelancerId());
    }

    /**
     * Checks if the authenticated user has access to the proposal.
     *
     * @param proposalCreationDTO the proposal DTO
     * @param auth                the authentication object
     * @return true if the user has access, false otherwise
     */
    private static Boolean hasUserAccess(ProposalCreationDTO proposalCreationDTO, Authentication auth) {
        User user = ((UserDetails) auth.getPrincipal()).getUser();
        return user.isAdmin() || user.getId().equals(proposalCreationDTO.getFreelancerId());
    }
}
