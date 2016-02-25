/*
 * LunaticSMTP
 * Copyright (C) 2016  Anton Larionov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gitlab.anlar.lunatic;

import com.beust.jcommander.JCommander;
import com.gitlab.anlar.lunatic.gui.LunaticApplication;
import com.gitlab.anlar.lunatic.server.EmailServer;
import com.gitlab.anlar.lunatic.server.EmailWriter;
import com.gitlab.anlar.lunatic.server.StartResult;
import com.gitlab.anlar.lunatic.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lunatic {
    private static final Logger log = LoggerFactory.getLogger(Lunatic.class);

    public static void main(String[] args) {
        Config config = Config.getInstance();
        JCommander commander = new JCommander(config, args);
        commander.setProgramName("java -jar LunaticSMTP.jar");

        if (config.isHelp()) {
            printHelp(commander);
        } else {
            if (config.isNoGui()) {
                if (config.isStart()) {
                    startServer(config.getPort(), config.isWrite(), config.getDirectory());
                } else {
                    log.info("Skip SMTP server start (no-gui option should be combined with auto-start)");
                }
            } else {
                LunaticApplication.launch(LunaticApplication.class, args);
            }
        }
    }

    private static void printHelp(JCommander commander) {
        StringBuilder sb = new StringBuilder();
        commander.usage(sb);
        sb.insert(0, String.format("LunaticSMTP, version %s, revision %s\n", Version.getVersion(), Version.getGitShortRevision()));
        JCommander.getConsole().println(sb.toString());
    }

    private static void startServer(int port, boolean isWrite, String directory) {
        EmailServer.initEmailWriter(new EmailWriter.Config() {
            @Override
            public boolean isActive() {
                return isWrite;
            }

            @Override
            public String getDirectory() {
                return directory;
            }
        });

        StartResult result = EmailServer.start(port);

        if (!result.isSuccessful()) {
            log.error("Failed to start SMTP server, {}", result.getMessage());
        }
    }
}
