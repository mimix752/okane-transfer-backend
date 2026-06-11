package com.okanetransfer.service;

import java.util.Optional;

public interface EmbeddingService {


    Optional<String> embed(String text);
}