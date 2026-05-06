package com.br.food.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.models.Address;
import com.br.food.models.CompanyOpeningHour;
import com.br.food.models.CompanyProfile;
import com.br.food.models.Document;
import com.br.food.repository.CompanyProfileRepository;
import com.br.food.request.AddressRequest;
import com.br.food.request.CompanyProfileRequest;
import com.br.food.request.OpeningHourRequest;
import com.br.food.response.CompanyProfileResponse;

@Service
public class CompanyProfileService {

	public static final String COMPANY_PROFILE_CACHE = "companyProfileMenu";

	private final CompanyProfileRepository companyProfileRepository;
	private final DocumentService documentService;

	public CompanyProfileService(CompanyProfileRepository companyProfileRepository, DocumentService documentService) {
		this.companyProfileRepository = companyProfileRepository;
		this.documentService = documentService;
	}

	@Transactional(readOnly = true)
	public CompanyProfile findCurrent() {
		return companyProfileRepository.findFirstByOrderByIdAsc().orElse(null);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = COMPANY_PROFILE_CACHE, key = "'current'")
	public CompanyProfileResponse findCurrentResponse() {
		CompanyProfile companyProfile = findCurrent();
		return companyProfile != null ? new CompanyProfileResponse(companyProfile) : null;
	}

	@Transactional
	@CacheEvict(cacheNames = COMPANY_PROFILE_CACHE, allEntries = true)
	public CompanyProfile upsert(CompanyProfileRequest request, MultipartFile logo, MultipartFile banner) throws IOException {
		validateOpeningHours(request.getOpeningHours());

		CompanyProfile profile = companyProfileRepository.findFirstByOrderByIdAsc().orElseGet(CompanyProfile::new);
		profile.setCompanyName(request.getCompanyName());
		profile.setSlogan(request.getSlogan());
		profile.setPrimaryColor(request.getPrimaryColor());
		profile.setDigitalOrderingEnabled(request.getDigitalOrderingEnabled());
		profile.setDineInEnabled(request.getDineInEnabled());
		profile.setDeliveryEnabled(request.getDeliveryEnabled());
		profile.setTakeawayEnabled(request.getTakeawayEnabled());
		profile.setWhatsappNumber(request.getWhatsappNumber());
		profile.setAddress(buildAddress(profile.getAddress(), request.getAddress()));

		Document logoDocument = documentService.convertToDocument(logo);
		if (logoDocument != null) {
			profile.setLogo(logoDocument);
		}

		Document bannerDocument = documentService.convertToDocument(banner);
		if (bannerDocument != null) {
			profile.setBanner(bannerDocument);
		}

		profile.getOpeningHours().clear();
		for (OpeningHourRequest openingHourRequest : request.getOpeningHours()) {
			CompanyOpeningHour openingHour = new CompanyOpeningHour();
			openingHour.setCompanyProfile(profile);
			openingHour.setDayOfWeek(openingHourRequest.getDayOfWeek());
			openingHour.setOpenTime(openingHourRequest.getOpenTime());
			openingHour.setCloseTime(openingHourRequest.getCloseTime());
			openingHour.setActive(openingHourRequest.getActive() != null ? openingHourRequest.getActive() : Boolean.TRUE);
			profile.getOpeningHours().add(openingHour);
		}

		profile.getOpeningHours().sort(Comparator.comparing(CompanyOpeningHour::getDayOfWeek));
		return companyProfileRepository.save(profile);
	}

	private Address buildAddress(Address currentAddress, AddressRequest request) {
		Address address = currentAddress != null ? currentAddress : new Address();
		address.setStreet(request.getStreet());
		address.setNumber(request.getNumber());
		address.setDistrict(request.getDistrict());
		address.setPostalCode(request.getPostalCode());
		address.setCity(request.getCity());
		address.setStatus(Boolean.TRUE);
		return address;
	}

	private void validateOpeningHours(List<OpeningHourRequest> openingHours) {
		Set<java.time.DayOfWeek> dayOfWeeks = new HashSet<>();
		for (OpeningHourRequest openingHour : openingHours) {
//			if (!openingHour.getCloseTime().isAfter(openingHour.getOpenTime())) {
//				throw new DataIntegrityViolationException("Close time must be after open time.");
//			}
			if (!dayOfWeeks.add(openingHour.getDayOfWeek())) {
				throw new DataIntegrityViolationException("Apenas um horario de funcionamento por dia da semana e permitido.");
			}
		}
	}
}
