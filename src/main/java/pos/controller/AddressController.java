package pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pos.domain.Address;
import pos.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService service;

    @PostMapping
    public ResponseEntity<Address> create(@RequestBody @Valid Address address) {
        Address saved = service.create(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public Address get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Address> list(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            return service.listByUser(userId);
        }
        return service.list();
    }

    @PutMapping("/{id}")
    public Address update(@PathVariable Long id, @RequestBody @Valid Address address) {
        return service.update(id, address);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

