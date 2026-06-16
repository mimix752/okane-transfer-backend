package com.okanetransfer.service;

import com.okanetransfer.service.impl.SemanticFaqServiceImpl;

import java.util.Optional;

public interface SemanticFaqService {


    Optional<SemanticFaqServiceImpl.FaqResult>  findAnswer(String userMessage);
}