package com.github.johnsiu.binance.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.johnsiu.binance.models.Account.Balance;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Jackson deserializer for turning an array of balances into a map of asset to balance.
 */
public class BalanceMapDeserializer extends JsonDeserializer<Map<String, Balance>> {

  @Override
  public Map<String, Balance> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    List<Balance> balances = p.readValueAs(new TypeReference<List<Balance>>() {
    });
    Builder<String, Balance> builder = ImmutableMap.<String, Balance>builder();
    balances.forEach(balance -> builder.put(balance.getAsset(), balance));
    return builder.build();
  }
}
