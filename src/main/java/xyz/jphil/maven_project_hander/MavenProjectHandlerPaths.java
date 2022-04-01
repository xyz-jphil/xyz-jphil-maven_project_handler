/*
 * Copyright 2022 Ivan Velikanov evanvelikanov@gmail.com.
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import static xyz.jphil.maven_project_hander.OpenButton.Action.*;
import xyz.jphil.patterns.lambda.L;

/**
 *
 * @author Ivan Velikanov evanvelikanov@gmail.com
 */
public class MavenProjectHandlerPaths {
    public static Path home(){
        var p = Paths.get(System.getProperty("user.home")).resolve(".jphil/xyz-jphil-maven_project_handler");
        try {
            if(!Files.exists(p)){
                Files.createDirectories(p);
            }
        } catch (Exception e) {
        }
        return p;
    }
    
    public static String property(Enum<?> e){
        return properties().getProperty(e.name());
    }
    
    public static Path path(String p){
        return Paths.get(properties().getProperty(p));
    }
    
    public static int intVal(String p, int defaultV){
        return L.ifError(()->Integer.parseInt(properties().getProperty(p)),defaultV,L.D);
    }
    
    public static Properties properties(){
        Properties p = new Properties();
        p.setProperty(OPENNB.name(), "c:\\Program Files\\NetBeans-13\\netbeans\\bin\\netbeans64.exe");
        p.setProperty(OPENINTELLIJ.name(), "idea64");
        p.setProperty("codedirscount", "1");
        p.setProperty("codedir1", "c:\\code");
        Path s = home().resolve("settings.properties");
        
        if(!Files.exists(s)){
            try {
                p.store(new FileOutputStream(s.toFile()), "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try{
            p.load(new FileInputStream(s.toFile()));
        }catch(Exception a){
            try {
                p.store(new FileOutputStream(s.toFile()), "");
            } catch (Exception e) {
                e.addSuppressed(e);
            }
            a.printStackTrace();
        }
        return p;
    }
}
