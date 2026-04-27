package com.bank.banking_service;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectPackages;

@Suite
@SelectPackages({
        "com.bank.models",
        "com.bank.repository",
        "com.bank.banking_service.service",
        "com.bank.util"
})
public class TestRunner {
}