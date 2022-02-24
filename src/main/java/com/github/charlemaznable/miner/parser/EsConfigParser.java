package com.github.charlemaznable.miner.parser;

import com.github.charlemaznable.es.diamond.EsConfigDiamondElf;
import com.github.charlemaznable.miner.MinerStoneParse.MinerStoneParser;

public final class EsConfigParser implements MinerStoneParser {

    @Override
    public Object parse(String stone, Class<?> clazz) {
        return EsConfigDiamondElf.parseStoneToEsConfig(stone);
    }
}
