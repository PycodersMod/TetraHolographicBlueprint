package com.pycoder.tetraholographicblueprint.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProcessingConditionSummary {
    private final Map<String, Integer> minimumToolLevels = new LinkedHashMap<>();
    private final Map<String, ProcessingCondition> conditions = new LinkedHashMap<>();

    public void addToolRequirement(String actionKey, int minimumLevel) {
        Objects.requireNonNull(actionKey, "actionKey");
        if (minimumLevel <= 0) {
            return;
        }
        minimumToolLevels.merge(actionKey, minimumLevel, Math::max);
    }

    public void addCondition(ProcessingConditionKind kind, String identity, String displayText) {
        ProcessingCondition condition = new ProcessingCondition(kind, identity, displayText);
        String deduplicationKey = kind.name() + "\u0000" + identity;
        conditions.putIfAbsent(deduplicationKey, condition);
    }

    public Map<String, Integer> minimumToolLevels() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(minimumToolLevels));
    }

    public List<ProcessingCondition> conditions() {
        return Collections.unmodifiableList(new ArrayList<>(conditions.values()));
    }

    public void clear() {
        minimumToolLevels.clear();
        conditions.clear();
    }

    public ProcessingConditionSummary copy() {
        ProcessingConditionSummary copy = new ProcessingConditionSummary();
        for (Map.Entry<String, Integer> entry : minimumToolLevels().entrySet()) {
            copy.addToolRequirement(entry.getKey(), entry.getValue());
        }
        for (ProcessingCondition condition : conditions()) {
            copy.addCondition(condition.kind(), condition.identity(), condition.displayText());
        }
        return copy;
    }
}
