package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.Product;
import java.util.List;
import java.util.ArrayList;

@Service
public class MenuService {

    public List<Product> list() {
        return new ArrayList<>();
    }

    public List<Product> byCategory(String category) {
        return new ArrayList<>();
    }
}
