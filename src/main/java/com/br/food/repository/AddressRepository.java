package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

}
