/**
 * The MIT License
 * Copyright © 2019 FAIR Data Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package solutions.fairdata.openrefine.metadata.commands;

import com.google.refine.commands.Command;
import solutions.fairdata.openrefine.metadata.MetadataModuleImpl;
import solutions.fairdata.openrefine.metadata.commands.request.auth.AuthRequest;
import solutions.fairdata.openrefine.metadata.commands.response.auth.AuthResponse;
import solutions.fairdata.openrefine.metadata.commands.response.ErrorResponse;
import solutions.fairdata.openrefine.metadata.dto.auth.AuthDTO;
import solutions.fairdata.openrefine.metadata.dto.auth.TokenDTO;
import solutions.fairdata.openrefine.metadata.dto.config.FDPConnectionConfigDTO;
import solutions.fairdata.openrefine.metadata.fdp.FairDataPointClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class AuthCommand extends Command {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = CommandUtils.objectMapper.readValue(request.getReader(), AuthRequest.class);
        Writer w = CommandUtils.prepareWriter(response);

        try {
            AuthDTO authDTO = authRequest.getAuthDTO();
            String fdpUri = authRequest.getFdpUri();
            if (authRequest.isConfiguredMode()) {
                FDPConnectionConfigDTO fdpConnectionConfigDTO = MetadataModuleImpl.getInstance().getSettings().getFdpConnections().get(authRequest.getConfigId());
                authDTO.setEmail(fdpConnectionConfigDTO.getEmail());
                authDTO.setPassword(fdpConnectionConfigDTO.getPassword());
                fdpUri = fdpConnectionConfigDTO.getBaseURI();
            } else if (authRequest.isCustomMode() && !MetadataModuleImpl.getInstance().getSettings().getAllowCustomFDP()) {
                throw new IOException("Custom FDP connection is not allowed!");
            }

            FairDataPointClient fdpClient = new FairDataPointClient(fdpUri, logger);
            TokenDTO tokenDTO = fdpClient.postAuthentication(authDTO);

            CommandUtils.objectMapper.writeValue(w, new AuthResponse(tokenDTO.getToken(), fdpUri));
        } catch (Exception e) {
            logger.error("Error while authenticating with FAIR Data Point: " + authRequest.getFdpUri() + " (" + e.getMessage() + ")");
            CommandUtils.objectMapper.writeValue(w, new ErrorResponse("auth-fdp-command/error", e));
        } finally {
            w.flush();
            w.close();
        }

    }
}
