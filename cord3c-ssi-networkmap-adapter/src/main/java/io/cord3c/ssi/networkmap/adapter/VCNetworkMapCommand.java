package io.cord3c.ssi.networkmap.adapter;

import com.github.rmee.boot.cli.command.ApplicationContainerCommand;
import com.github.rmee.boot.cli.command.spring.SpringRunCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "", sortOptions = false)
public class VCNetworkMapCommand extends ApplicationContainerCommand {

	public VCNetworkMapCommand() {
		addCommand("run", new SpringRunCommand());
	}
}
