package com.elgregos.java.hazelcast.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.elgregos.java.hazelcast.aspect.LogTime;
import com.elgregos.java.hazelcast.entities.hierarchy.HierarchyValue;
import com.elgregos.java.hazelcast.service.HierarchyService;
import com.elgregos.java.hazelcast.service.HierarchyValueService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component
public class HierarchyValueCache {

	private static final String HIERARCHY_VALUES_ENTITIES = "hierarchy-values-entities";

	@Autowired
	private HierarchyService hierarchyService;

	@Autowired
	private HierarchyValueService hierarchyValueService;

	@Autowired
	private HazelcastInstance hazelcastInstance;

	private IMap<Long, HierarchyValue> map;

	public void clear() {
		map.clear();
	}

	@LogTime
	public HierarchyValue get(Long id) {
		return map.get(id);
	}

	@LogTime
	public List<HierarchyValue> getWithMultiGet(List<Long> randomIds) {
		final List<HierarchyValue> hierarchyValues = new ArrayList<>(randomIds.size());
		for (final Long id : randomIds) {
			hierarchyValues.add(map.get(id));
		}
		return hierarchyValues;
	}

	@LogTime
	public List<HierarchyValue> getWithOneGet(Set<Long> randomIds) {
		return new ArrayList<>(map.getAll(randomIds).values());
	}

	@PostConstruct
	public void init() {
		map = hazelcastInstance.getMap(HIERARCHY_VALUES_ENTITIES);
	}

	@LogTime
	public void loadCache() {
		hierarchyService.getAllHierarchyCodes().forEach(hierarchyCode -> loadCache(hierarchyCode));
	}

	private void loadCache(String hierarchyCode) {
		final List<HierarchyValue> hierarchyValues = hierarchyValueService.findByHierarchyCode(hierarchyCode);
		final Map<Long, HierarchyValue> mapById = hierarchyValues.stream()
				.collect(Collectors.toMap(HierarchyValue::getId, Function.identity()));
		map.putAll(mapById);
	}

}
