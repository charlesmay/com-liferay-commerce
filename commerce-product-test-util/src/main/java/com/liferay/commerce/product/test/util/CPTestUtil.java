/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.product.test.util;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.commerce.product.configuration.CPOptionConfiguration;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CPInstanceConstants;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CPOptionValue;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalServiceUtil;
import com.liferay.commerce.product.service.CPDefinitionOptionRelLocalServiceUtil;
import com.liferay.commerce.product.service.CPInstanceLocalServiceUtil;
import com.liferay.commerce.product.service.CPOptionLocalServiceUtil;
import com.liferay.commerce.product.service.CPOptionValueLocalServiceUtil;
import com.liferay.commerce.product.service.CommerceCatalogLocalServiceUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Andrea Di Giorgi
 * @author Luca Pellizzon
 */
public class CPTestUtil {

	public static AssetCategory addCategoryToCPDefinitions(
			long groupId, long... cpDefinitionIds)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		AssetVocabulary assetVocabulary =
			AssetVocabularyLocalServiceUtil.addVocabulary(
				serviceContext.getUserId(), groupId,
				RandomTestUtil.randomString(), serviceContext);

		AssetCategory assetCategory = AssetCategoryLocalServiceUtil.addCategory(
			serviceContext.getUserId(), groupId, RandomTestUtil.randomString(),
			assetVocabulary.getVocabularyId(), serviceContext);

		serviceContext.setAssetCategoryIds(
			new long[] {assetCategory.getCategoryId()});

		for (long cpDefinitionId : cpDefinitionIds) {
			CPDefinitionLocalServiceUtil.updateCPDefinitionCategorization(
				cpDefinitionId, serviceContext);
		}

		return assetCategory;
	}

	public static CPDefinition addCPDefinition(
			long groupId, boolean ignoreSKUCombinations,
			boolean hasDefaultInstance, int workflowAction)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		serviceContext.setWorkflowAction(workflowAction);

		return _addCPDefinition(
			SimpleCPTypeConstants.NAME, ignoreSKUCombinations,
			hasDefaultInstance, serviceContext);
	}

	public static CPDefinition addCPDefinition(
			long groupId, String productTypeName, boolean ignoreSKUCombinations,
			boolean hasDefaultInstance)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		return _addCPDefinition(
			productTypeName, ignoreSKUCombinations, hasDefaultInstance,
			serviceContext);
	}

	public static CPDefinitionOptionRel addCPDefinitionOptionRel(
			long groupId, long cpDefinitionId, long cpOptionId)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		return CPDefinitionOptionRelLocalServiceUtil.addCPDefinitionOptionRel(
			cpDefinitionId, cpOptionId, true, serviceContext);
	}

	public static CPInstance addCPInstance(long groupId)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		CPDefinition cpDefinition = _addCPDefinition(
			SimpleCPTypeConstants.NAME, true, true, serviceContext);

		return CPInstanceLocalServiceUtil.getCPInstance(
			cpDefinition.getCPDefinitionId(), CPInstanceConstants.DEFAULT_SKU);
	}

	public static CPOption addCPOption(long groupId, boolean skuContributor)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(groupId);

		CPOptionConfiguration cpOptionConfiguration =
			ConfigurationProviderUtil.getConfiguration(
				CPOptionConfiguration.class,
				new SystemSettingsLocator(CPConstants.CP_OPTION_SERVICE_NAME));

		String[] ddmFormFieldTypesAllowed =
			cpOptionConfiguration.ddmFormFieldTypesAllowed();

		return CPOptionLocalServiceUtil.addCPOption(
			serviceContext.getUserId(), RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(), ddmFormFieldTypesAllowed[0],
			RandomTestUtil.randomBoolean(), RandomTestUtil.randomBoolean(),
			skuContributor, RandomTestUtil.randomString(), null,
			serviceContext);
	}

	public static CPOptionValue addCPOptionValue(CPOption cpOption)
		throws PortalException {

		return CPOptionValueLocalServiceUtil.addCPOptionValue(
			cpOption.getCPOptionId(), RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomDouble(), RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext());
	}

	public static void buildCPInstances(CPDefinition cpDefinition)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(cpDefinition.getGroupId());

		CPInstanceLocalServiceUtil.buildCPInstances(
			cpDefinition.getCPDefinitionId(), serviceContext);
	}

	public static SearchContext getSearchContext(
		String keywords, int status, Group group) {

		SearchContext searchContext = new SearchContext();

		Map<String, Serializable> attributes = new HashMap<>();

		attributes.put(Field.STATUS, status);

		LinkedHashMap<String, Object> params = new LinkedHashMap<>();

		if (Validator.isNotNull(keywords)) {
			params.put("keywords", keywords);
		}
		else {
			params.put("keywords", StringPool.STAR);
		}

		attributes.put("params", params);

		searchContext.setAttributes(attributes);

		searchContext.setCompanyId(group.getCompanyId());
		searchContext.setGroupIds(new long[] {group.getGroupId()});

		if (Validator.isNotNull(keywords)) {
			searchContext.setKeywords(keywords);
		}
		else {
			searchContext.setKeywords(StringPool.STAR);
		}

		return searchContext;
	}

	private static CPDefinition _addCPDefinition(
			String productTypeName, boolean ignoreSKUCombinations,
			boolean hasDefaultInstance, ServiceContext serviceContext)
		throws PortalException {

		User user = UserLocalServiceUtil.getUser(serviceContext.getUserId());

		List<CommerceCatalog> commerceCatalogs =
			CommerceCatalogLocalServiceUtil.getCommerceCatalogs(
				user.getCompanyId(), true);

		CommerceCatalog commerceCatalog = commerceCatalogs.get(0);

		long now = System.currentTimeMillis();

		Map<Locale, String> titleMap = RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> shortDescriptionMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> descriptionMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> metaTitleMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> metaKeywordsMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> metaDescriptionMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> urlTitleMap =
			RandomTestUtil.randomLocaleStringMap();
		boolean shippable = RandomTestUtil.randomBoolean();
		boolean freeShipping = RandomTestUtil.randomBoolean();
		boolean shipSeparately = RandomTestUtil.randomBoolean();
		double shippingExtraPrice = RandomTestUtil.randomDouble();
		double width = RandomTestUtil.randomDouble();
		double height = RandomTestUtil.randomDouble();
		double depth = RandomTestUtil.randomDouble();
		double weight = RandomTestUtil.randomDouble();
		long cpTaxCategoryId = 0;
		boolean taxExempt = RandomTestUtil.randomBoolean();
		boolean telcoOrElectronics = RandomTestUtil.randomBoolean();
		String ddmStructureKey = null;
		boolean published = true;

		Date displayDate = new Date(now - Time.HOUR);
		Date expirationDate = new Date(now + Time.DAY);

		Calendar displayCal = CalendarFactoryUtil.getCalendar(
			user.getTimeZone());

		displayCal.setTime(displayDate);

		int displayDateMonth = displayCal.get(Calendar.MONTH);
		int displayDateDay = displayCal.get(Calendar.DATE);
		int displayDateYear = displayCal.get(Calendar.YEAR);
		int displayDateHour = displayCal.get(Calendar.HOUR);
		int displayDateMinute = displayCal.get(Calendar.MINUTE);

		if (displayCal.get(Calendar.AM_PM) == Calendar.PM) {
			displayDateHour += 12;
		}

		Calendar expirationCal = CalendarFactoryUtil.getCalendar(
			user.getTimeZone());

		expirationCal.setTime(expirationDate);

		int expirationDateMonth = expirationCal.get(Calendar.MONTH);
		int expirationDateDay = expirationCal.get(Calendar.DATE);
		int expirationDateYear = expirationCal.get(Calendar.YEAR);
		int expirationDateHour = expirationCal.get(Calendar.HOUR);
		int expirationDateMinute = expirationCal.get(Calendar.MINUTE);

		if (expirationCal.get(Calendar.AM_PM) == Calendar.PM) {
			expirationDateHour += 12;
		}

		String defaultSku = null;

		if (hasDefaultInstance) {
			defaultSku = CPInstanceConstants.DEFAULT_SKU;
		}

		return CPDefinitionLocalServiceUtil.addCPDefinition(
			commerceCatalog.getGroupId(), user.getUserId(), titleMap,
			shortDescriptionMap, descriptionMap, urlTitleMap, metaTitleMap,
			metaKeywordsMap, metaDescriptionMap, productTypeName,
			ignoreSKUCombinations, shippable, freeShipping, shipSeparately,
			shippingExtraPrice, width, height, depth, weight, cpTaxCategoryId,
			taxExempt, telcoOrElectronics, ddmStructureKey, published,
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, expirationDateMonth, expirationDateDay,
			expirationDateYear, expirationDateHour, expirationDateMinute, false,
			defaultSku, false, 0, null, null, 0L, null, serviceContext);
	}

}