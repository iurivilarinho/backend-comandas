package com.br.food.service;

import java.net.URI;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.PushSubscription;
import com.br.food.repository.PushSubscriptionRepository;
import com.br.food.request.PushSubscriptionRequest;
import com.br.food.response.PushDiagnosticsResponse;
import com.br.food.response.PushTestResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;

@Service
public class PushNotificationService {

	private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

	private final PushSubscriptionRepository subscriptionRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${push.vapid.public-key:}")
	private String publicKey;

	@Value("${push.vapid.private-key:}")
	private String privateKey;

	@Value("${push.vapid.subject:mailto:no-reply@comandas.local}")
	private String subject;

	private PushService pushService;

	public PushNotificationService(PushSubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}

	@PostConstruct
	private void init() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		if (!isConfigured()) {
			log.warn("[push] VAPID keys ausentes — push notifications desativado. "
					+ "Defina PUSH_VAPID_PUBLIC_KEY e PUSH_VAPID_PRIVATE_KEY.");
			return;
		}

		try {
			this.pushService = new PushService(publicKey, privateKey, subject);
			log.info("[push] PushService inicializado com VAPID. subject={}, publicKey={}…",
					subject, publicKey.substring(0, Math.min(12, publicKey.length())));
		} catch (Exception exception) {
			log.error("[push] falha ao inicializar PushService", exception);
			this.pushService = null;
		}
	}

	public boolean isConfigured() {
		return publicKey != null && !publicKey.isBlank() && privateKey != null && !privateKey.isBlank();
	}

	public String getPublicKey() {
		return publicKey;
	}

	@Transactional
	public PushSubscription saveSubscription(PushSubscriptionRequest request) {
		PushSubscription saved = subscriptionRepository
				.findByEndpoint(request.getEndpoint())
				.map(existing -> {
					existing.update(request.getP256dh(), request.getAuth(), request.getTopic(), request.getCustomerId());
					return existing;
				})
				.orElseGet(() -> subscriptionRepository.save(new PushSubscription(
						request.getEndpoint(),
						request.getP256dh(),
						request.getAuth(),
						request.getTopic(),
						request.getCustomerId())));
		log.info("[push] subscription salva id={} topic={} customerId={} endpointStart={}",
				saved.getId(),
				saved.getTopic(),
				saved.getCustomerId(),
				saved.getEndpoint().substring(0, Math.min(50, saved.getEndpoint().length())));
		return saved;
	}

	@Transactional
	public void deleteSubscription(String endpoint) {
		subscriptionRepository.findByEndpoint(endpoint).ifPresent(record -> {
			subscriptionRepository.delete(record);
			log.info("[push] subscription removida id={} topic={}", record.getId(), record.getTopic());
		});
	}

	public void notifyTopic(String topic, String title, String body, String url) {
		if (pushService == null) {
			log.warn("[push] notifyTopic ignorado (pushService=null) topic={} title={}", topic, title);
			return;
		}
		List<PushSubscription> subscriptions = subscriptionRepository.findByTopic(topic);
		log.info("[push] notifyTopic topic={} subscriptions={} title={}",
				topic, subscriptions.size(), title);
		dispatch(subscriptions, title, body, url);
	}

	public void notifyCustomer(Long customerId, String title, String body, String url) {
		if (pushService == null) {
			log.warn("[push] notifyCustomer ignorado (pushService=null) customerId={} title={}",
					customerId, title);
			return;
		}
		if (customerId == null) {
			log.warn("[push] notifyCustomer chamado sem customerId, ignorando.");
			return;
		}
		List<PushSubscription> subscriptions = subscriptionRepository.findByCustomerId(customerId);
		log.info("[push] notifyCustomer customerId={} subscriptions={} title={}",
				customerId, subscriptions.size(), title);
		dispatch(subscriptions, title, body, url);
	}

	@Transactional(readOnly = true)
	public PushDiagnosticsResponse getDiagnostics(String endpoint) {
		Map<String, Long> counts = new HashMap<>();
		counts.put(PushSubscription.TOPIC_KITCHEN,
				(long) subscriptionRepository.findByTopic(PushSubscription.TOPIC_KITCHEN).size());
		counts.put(PushSubscription.TOPIC_CUSTOMER,
				(long) subscriptionRepository.findByTopic(PushSubscription.TOPIC_CUSTOMER).size());
		counts.put(PushSubscription.TOPIC_TABLES,
				(long) subscriptionRepository.findByTopic(PushSubscription.TOPIC_TABLES).size());

		PushDiagnosticsResponse.SubscriptionInfo currentDevice = null;
		if (endpoint != null && !endpoint.isBlank()) {
			currentDevice = subscriptionRepository.findByEndpoint(endpoint)
					.map(record -> new PushDiagnosticsResponse.SubscriptionInfo(
							record.getId(),
							record.getTopic(),
							record.getCustomerId(),
							extractHost(record.getEndpoint())))
					.orElse(null);
		}

		return new PushDiagnosticsResponse(isConfigured(), counts, currentDevice);
	}

	public PushTestResponse sendTestToEndpoint(String endpoint) {
		if (!isConfigured()) {
			return new PushTestResponse(false, null,
					"VAPID keys ausentes — push desativado.", null, null);
		}
		if (endpoint == null || endpoint.isBlank()) {
			return new PushTestResponse(false, null, "Endpoint nao informado.", null, null);
		}

		PushSubscription record = subscriptionRepository.findByEndpoint(endpoint).orElse(null);
		if (record == null) {
			return new PushTestResponse(false, null,
					"Subscription nao encontrada no servidor para este dispositivo.", null, null);
		}

		String payload;
		try {
			payload = objectMapper.writeValueAsString(Map.of(
					"title", "Teste de notificacao",
					"body", "Se voce esta vendo isso, o push chegou neste dispositivo.",
					"url", "/admin/diagnostico"));
		} catch (Exception exception) {
			log.error("[push] falha serializando payload de teste", exception);
			return new PushTestResponse(true, null,
					"Falha ao serializar payload: " + exception.getMessage(),
					record.getId(), record.getTopic());
		}

		try {
			Subscription.Keys keys = new Subscription.Keys(record.getP256dh(), record.getAuth());
			Subscription subscription = new Subscription(record.getEndpoint(), keys);
			Notification notification = new Notification(subscription, payload);
			int statusCode = pushService.send(notification).getStatusLine().getStatusCode();
			log.info("[push] teste entregue id={} status={}", record.getId(), statusCode);
			if (isStaleSubscriptionStatus(statusCode)) {
				log.info("[push] teste indicou subscription invalida (status={}), removendo id={}",
						statusCode, record.getId());
				subscriptionRepository.delete(record);
			}
			return new PushTestResponse(true, statusCode, null, record.getId(), record.getTopic());
		} catch (Exception exception) {
			log.error("[push] falha entregando teste id={}", record.getId(), exception);
			return new PushTestResponse(true, null,
					exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName(),
					record.getId(), record.getTopic());
		}
	}

	private boolean isStaleSubscriptionStatus(int statusCode) {
		// 404/410: gateway nao reconhece mais. 403: VAPID atual nao bate com a usada
		// no momento da inscricao — subscription nao serve mais, removemos para forcar
		// re-assinatura no proximo toggle.
		return statusCode == 403 || statusCode == 404 || statusCode == 410;
	}

	private String extractHost(String endpoint) {
		try {
			return URI.create(endpoint).getHost();
		} catch (Exception exception) {
			return null;
		}
	}

	private void dispatch(List<PushSubscription> subscriptions, String title, String body, String url) {
		if (subscriptions.isEmpty()) {
			return;
		}

		String payload;
		try {
			payload = objectMapper.writeValueAsString(Map.of(
					"title", title,
					"body", body,
					"url", url == null ? "/" : url));
		} catch (Exception exception) {
			log.error("[push] falha serializando payload", exception);
			return;
		}

		for (PushSubscription record : subscriptions) {
			try {
				Subscription.Keys keys = new Subscription.Keys(record.getP256dh(), record.getAuth());
				Subscription subscription = new Subscription(record.getEndpoint(), keys);
				Notification notification = new Notification(subscription, payload);
				int statusCode = pushService.send(notification).getStatusLine().getStatusCode();
				log.info("[push] entregue id={} status={}", record.getId(), statusCode);
				if (isStaleSubscriptionStatus(statusCode)) {
					log.info("[push] subscription invalida (status={}), removendo id={}",
							statusCode, record.getId());
					subscriptionRepository.delete(record);
				}
			} catch (Exception exception) {
				log.error("[push] falha ao entregar id={} endpoint={}",
						record.getId(),
						record.getEndpoint().substring(0, Math.min(60, record.getEndpoint().length())),
						exception);
			}
		}
	}
}
