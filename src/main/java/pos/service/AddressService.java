package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pos.domain.Address;
import pos.domain.User;
import pos.repository.AddressRepository;
import pos.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public Address create(Address address) {
        try {
            if (address.getId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New address must not have an id");
            }
            if (address.getUser() == null || address.getUser().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address must reference a valid user (user.id)");
            }

            User owner = userRepository.findById(address.getUser().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found id=" + address.getUser().getId()
                    ));

            address.setUser(owner);
            Address saved = addressRepository.save(address);
            log.info("Address created id={} for userId={}", saved.getId(), owner.getId());
            return saved;

        } catch (ResponseStatusException ex) {
            // erros de validação/negócio: apenas propaga
            log.warn("Create address failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error creating address for userId={}",
                    address != null && address.getUser() != null ? address.getUser().getId() : null, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while creating address");
        } catch (RuntimeException ex) {
            log.error("Unexpected error creating address", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while creating address");
        }
    }

    @Transactional(readOnly = true)
    public Address get(Long id) {
        try {
            return addressRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found id=" + id));
        } catch (DataAccessException ex) {
            log.error("DB error reading address id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while fetching address");
        } catch (RuntimeException ex) {
            log.error("Unexpected error reading address id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while fetching address");
        }
    }

    @Transactional(readOnly = true)
    public List<Address> list() {
        try {
            return addressRepository.findAll();
        } catch (DataAccessException ex) {
            log.error("DB error listing addresses", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while listing addresses");
        } catch (RuntimeException ex) {
            log.error("Unexpected error listing addresses", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while listing addresses");
        }
    }

    @Transactional(readOnly = true)
    public List<Address> listByUser(Long userId) {
        try {
            // opcional: garantir que o usuário existe para retornar 404 mais semântico
            if (!userRepository.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found id=" + userId);
            }
            return addressRepository.findByUserId(userId);
        } catch (ResponseStatusException ex) {
            log.warn("List addresses by user failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error listing addresses for userId={}", userId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while listing addresses by user");
        } catch (RuntimeException ex) {
            log.error("Unexpected error listing addresses for userId={}", userId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while listing addresses by user");
        }
    }

    public Address update(Long id, Address payload) {
        try {
            Address a = addressRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found id=" + id));

            // não permitir troca de dono no update (boa prática)
            if (payload.getUser() != null && payload.getUser().getId() != null
                    && !payload.getUser().getId().equals(a.getUser().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change address owner (user)");
            }

            a.setStreet(payload.getStreet());
            a.setNumber(payload.getNumber());
            a.setComplement(payload.getComplement());
            a.setCity(payload.getCity());
            a.setState(payload.getState());
            a.setPostalCode(payload.getPostalCode());
            a.setCountry(payload.getCountry());
            a.setReference(payload.getReference());

            // JPA vai flushar ao fim da transação
            log.info("Address updated id={}", id);
            return a;

        } catch (ResponseStatusException ex) {
            log.warn("Update address failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error updating address id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while updating address");
        } catch (RuntimeException ex) {
            log.error("Unexpected error updating address id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while updating address");
        }
    }

    public void delete(Long id) {
        try {
            Address a = addressRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found id=" + id));
            addressRepository.delete(a);
            log.info("Address deleted id={}", id);

        } catch (ResponseStatusException ex) {
            log.warn("Delete address failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error deleting address id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while deleting address");
        } catch (RuntimeException ex) {
            log.error("Unexpected error deleting address id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while deleting address");
        }
    }
}
