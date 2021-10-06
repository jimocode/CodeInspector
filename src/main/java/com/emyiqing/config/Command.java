package com.emyiqing.config;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Command {
    @Parameter(names = {"-h", "--help"}, description = "Help Info", help = true)
    public boolean help;

    @Parameter(names = {"-f", "--file"}, description = "Scan Jar File")
    public List<String> files;
}
