/*
 * Copyright 2021 Ivan Velikanov evanvelikanov@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jphil.maven_project_hander;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static xyz.jphil.patterns.lambda.L.*;

/**
 *
 * @author Ivan Velikanov evanvelikanov@gmail.com
 */
public final class GitDir{
    private final Path dir;
    private final String cnf;
    
    private static final Pattern
            repoP = compile("url \\= .*\\:(.*)\\/(.*)\\.git"),
            userP = compile("name \\= (.*)")
    ;
    
    public GitDir(Path dir) {
        this.dir = dir;
        this.cnf = (dir==null)?"":ifError(()->Files.readString(dir.resolve("config")),"",D);;
    }
    
    public String org(){
        return resultOf(repoP, 1);
    }
    
    public String repo(){
        return resultOf(repoP, 2);
    }
    
    public String user(){
        return resultOf(userP, 1);
    }
    
    private String resultOf(Pattern p, int group){
        if(dir==null)return "";
        var m = p.matcher(cnf);
        return m.find()?m.group(group):"";
    }
}