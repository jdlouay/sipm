package com.sipm.service;

import com.sipm.common.BusinessException;
import com.sipm.model.Customer;
import com.sipm.repository.CustomerRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new BusinessException("Un client avec cet email existe déjà", "DUPLICATE_EMAIL");
        }
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Client non trouvé", "CUSTOMER_NOT_FOUND"));
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        
        // Vérifier si l'email est déjà utilisé par un autre client
        if (customerDetails.getEmail() != null && 
            !customerDetails.getEmail().equals(customer.getEmail()) && 
            customerRepository.existsByEmail(customerDetails.getEmail())) {
            throw new BusinessException("Un client avec cet email existe déjà", "DUPLICATE_EMAIL");
        }

        // Mettre à jour les champs
        customer.setName(customerDetails.getName());
        customer.setFirstName(customerDetails.getFirstName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setCity(customerDetails.getCity());
        customer.setPostalCode(customerDetails.getPostalCode());
        customer.setCountry(customerDetails.getCountry());
        customer.setTaxId(customerDetails.getTaxId());
        customer.setCreditLimit(customerDetails.getCreditLimit());
        customer.setPaymentTerms(customerDetails.getPaymentTerms());
        customer.setActive(customerDetails.isActive());

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new BusinessException("Client non trouvé", "CUSTOMER_NOT_FOUND");
        }
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Customer> searchCustomers(String query, Pageable pageable) {
        Specification<Customer> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("taxId")), searchPattern)
                ));
            }
            
            predicates.add(criteriaBuilder.equal(root.get("active"), true));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return customerRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findByTaxId(String taxId) {
        return customerRepository.findByTaxId(taxId);
    }
} 