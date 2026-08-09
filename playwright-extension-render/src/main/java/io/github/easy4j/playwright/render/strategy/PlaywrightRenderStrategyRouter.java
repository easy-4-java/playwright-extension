package io.github.easy4j.playwright.render.strategy;

import io.github.easy4j.playwright.render.enums.RenderType;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Router that selects the appropriate rendering strategy based on request type.
 */
public class PlaywrightRenderStrategyRouter {

    private Map<RenderType, PlaywrightRenderStrategy> strategyMap = new HashMap<>();

    public PlaywrightRenderStrategyRouter(List<PlaywrightRenderStrategy> playwrightRenderStrategyList) {
        if (playwrightRenderStrategyList == null || playwrightRenderStrategyList.isEmpty()) throw new IllegalArgumentException("PlaywrightRenderStrategy list can not be empty");
        this.strategyMap = playwrightRenderStrategyList.stream()
                .collect(Collectors.toMap(PlaywrightRenderStrategy::getRenderType, strategy -> strategy));
    }

    public PlaywrightRenderStrategy route(RenderType type){
        return strategyMap.get(type);
    }

}
