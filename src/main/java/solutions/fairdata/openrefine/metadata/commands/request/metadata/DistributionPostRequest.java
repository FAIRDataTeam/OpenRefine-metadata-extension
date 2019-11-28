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
package solutions.fairdata.openrefine.metadata.commands.request.metadata;

import solutions.fairdata.openrefine.metadata.dto.metadata.DistributionDTO;

public class DistributionPostRequest {

    private String fdpUri;
    private String token;
    private DistributionDTO distribution;

    public DistributionPostRequest() {
    }

    public DistributionPostRequest(String fdpUri, String token, DistributionDTO distribution) {
        this.fdpUri = fdpUri;
        this.token = token;
        this.distribution = distribution;
    }

    public String getFdpUri() {
        return fdpUri;
    }

    public void setFdpUri(String fdpUri) {
        this.fdpUri = fdpUri;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DistributionDTO getDistribution() {
        return distribution;
    }

    public void setDistribution(DistributionDTO distribution) {
        this.distribution = distribution;
    }
}
