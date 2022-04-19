/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.mycompany.cochranelibcrawler;

import org.apache.http.HttpEntity; 
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient; 
import org.apache.http.impl.client.HttpClients; 
import org.apache.http.util.EntityUtils;

import java.io.IOException; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*; 

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 *
 * @author gonzalezron
 */
public class CochraneLibCrawler {
    
    public void printIt(Object p){
        System.out.println(p);    
    }
    
   //Output String returns re-built String by size 
    public static String wordWrap(String str, int size, String d){
   //String output is equal to empty "" or " " for tap space before blocks     
        String output = ""; 
        String [] words;
        int width = 0; 
        
        str = str.replaceAll("\\s+", " ");
        words = str.split(d);
    
       for (String word : words) {
            if (word.length() >= size) {
                //If it's not the first word, put it on a new line
                if (!output.isEmpty()) {
                    output += "\n";
                }
                output += word + " ";
                width = word.length();
            }
            else if ((width + word.length()) <= size) {
                output += word + " ";
                width += word.length() + 1;
            } else {
                output = output.substring(0, output.length() - 1);
                output += "\n" + word + " ";
                width = word.length() + 1;
            }
        }

        return output.substring(0, output.length() - 1);
    }
    //wordWrap call method
    public static String wordWrap(String text, int width) {
        return wordWrap(text, width," ");
    }
/*
     FORMATTING TIME STAMP
     source-format' represents format to be changed to Preferred format 
     STRING arguments *notice hyphens & Ms
 */
    public String changeDate(String date) throws Exception {
       try{ 
        
        SimpleDateFormat sdfSource = new SimpleDateFormat("dd MMMM yyyy");
        Date setDate = sdfSource.parse(date);
        SimpleDateFormat sdfPreferred = new SimpleDateFormat("yyyy-M-dd");
        date = sdfPreferred.format(setDate);
       
       }catch(ParseException pe){
            this.printIt(pe);
       }
       return date; 
    }
    
    //ShowContent uses Jsoup to Parse and Scrape links and Urls
    public void ShowContent(HttpEntity entity) throws Exception{
        
        if (entity!= null){
            Document doc = Jsoup.parse(EntityUtils.toString(entity)); 
            
            
            Elements searchResult = doc.getElementsByClass("search-results-item-body");
            String topic = doc.getElementsByClass("facet-pill secondary").text();
            String libUrl = doc.getElementsByClass("aux-footer-nav-link").attr("href");
            libUrl = libUrl.substring(0, libUrl.length()-1);
         /**
          * Topic and Url do not change 
          * lubUrl edits Url for concatenation --> http://onlineLibrary.Wiley.com/ 
          * 
          * for loop searches body for URLs/Title/Author/Date* 
          * 
          */
                for (Element item_body : searchResult){ 
                    
                    //String edit removed /cdsr/ path from link new substring
                    String hrefl = item_body.getElementsByTag("a").attr("href");
                    hrefl = hrefl.substring(5,hrefl.length());
                    
                    //string refDate formats date  month-name to month-number
                    String refDate = item_body.getElementsByClass("search-result-date").text();
                    refDate = this.changeDate(refDate);
                    
                    
                    this.printIt(wordWrap(libUrl + hrefl +"|"+ topic +"|"+
                    item_body.getElementsByClass("result-title").text()+"|"+
                    item_body.getElementsByClass("search-result-authors").text()+"|"+
                    refDate, 91) + "\n");
                    
                }                      
        }   
        
    }

public static void main(String[] args) throws IOException {
    
        CochraneLibCrawler crawler= new CochraneLibCrawler();        
        String cochraneLib = "http://www.cochranelibrary.com/home/topic-and-review-group-list.html?page=topic"; 
        ArrayList<String> arrayList = new ArrayList<>(); 
        Scanner scr = new Scanner(System.in);
                
      /*
        REQUEST AN HTTP: LINK 
        Add User-Agent Header for Authorization HTTP 419 ERROR 
        No other Headers required
        */
   
    try(CloseableHttpClient client = HttpClients.createDefault()){
        
        HttpGet fetchPage = new HttpGet(cochraneLib); 
        fetchPage.addHeader("User-Agent","Whatever");
        //USER AGENT MUST BEV PRESENT WITH EVERY REQUEST
       
        //TRY BLOCK RESPONSE IS A MENU OF TOPICS 
        try (CloseableHttpResponse response = client.execute(fetchPage)){   
            HttpEntity entity = response.getEntity();
            Document doc = Jsoup.parse(EntityUtils.toString(entity));

            Elements topics = doc.getElementsByClass("browse-by-list-item");
            
            //DISPLAYS NUMBERED TOPICS 
            int i = 0; 
            for (Element topic : topics ){  
                crawler.printIt("#"+ i++ +". "+topic.getElementsByTag("a").text());
                arrayList.add(topic.getElementsByTag("a").attr("href"));
                }
            
            String chosenTopic;         
            crawler.printIt("Enter the topic number:");
            String myInput = scr.nextLine();
  
            //FILTER FOR INPUT MUST NOT BE EMPTY OR MATCH LETTERS ELSE EXAMPLE DISPLAYS 
            if (myInput.isEmpty()!= true && myInput.matches("[a-zA-Z]") != true){
                
               chosenTopic = arrayList.get(Integer.parseInt(myInput));
               
            }else
                chosenTopic = arrayList.get(0);
               
                
            HttpGet browseByTopics = new HttpGet(chosenTopic);
            browseByTopics.addHeader("User-Agent", "Whatever");
               
                    arrayList.clear();
                    doc.empty();    
         /**
          * Try block first finds links
          * then crawls with for loop to gather information 
          */            
       try (CloseableHttpResponse response2 = client.execute(browseByTopics)){            
            
            HttpEntity entity2 = response2.getEntity();                  
            doc = Jsoup.parse(EntityUtils.toString(entity2)); 
           
            Elements pageHtml = doc.getElementsByClass("pagination-page-list-item");
       
            for(Element page : pageHtml ){
                String pagelinks = page.getElementsByTag("a").attr("href"); 
                arrayList.add(pagelinks);
            }
        } catch(Exception e){
            crawler.printIt(e);
        }  
 
       
       for ( String pageLink : arrayList){
           
           HttpGet getPage = new HttpGet(pageLink); 
           getPage.addHeader("User-Agent", "Any");
           
           try (CloseableHttpResponse responseData = client.execute(getPage)){
               HttpEntity entityData = responseData.getEntity(); 
               
                crawler.ShowContent(entityData);  
      
            }catch (Exception e){
              crawler.printIt(e);
            }       
       }
       
      
    }catch (Exception e){
        crawler.printIt(e);    
    }
       
    }
       
    }
}
    




