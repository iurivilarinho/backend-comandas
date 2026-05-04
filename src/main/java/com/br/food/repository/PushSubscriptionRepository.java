package com.br.food.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.PushSubscription;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

	List<PushSubscription> findByEndpoint(String endpoint);

	Optional<PushSubscription> findByEndpointAndTopic(String endpoint, String topic);

	List<PushSubscription> findByTopic(String topic);

	List<PushSubscription> findByCustomerId(Long customerId);
}
