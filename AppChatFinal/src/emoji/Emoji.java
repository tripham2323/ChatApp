/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package emoji;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author Admin
 */
public class Emoji {
    
    private static Emoji instance;
    public static Emoji getInstance(){
        if (instance == null) {
            instance = new Emoji();
        }
        return instance;
    }
   private Emoji(){
   
   }
   
   public List<Model_Emoji>getStyle1(){
         List<Model_Emoji> list = new ArrayList<>();
         for(int i=1; i<=89 ; i++){
            list.add(new Model_Emoji(i, new ImageIcon(getClass().getResource("/emoji/icon/"+i+".png"))).toSize(30, 30));
         
         }
         return list;
   }
   public List<Model_Emoji>getStyle2(){
         List<Model_Emoji> list = new ArrayList<>();
         for(int i=90; i<=113 ; i++){
            list.add(new Model_Emoji(i, new ImageIcon(getClass().getResource("/emoji/icon/"+i+".png"))).toSize(70, 72));
         
         }
         return list;
   }
   public List<Model_Emoji>getStyle3(){
         List<Model_Emoji> list = new ArrayList<>();
         for(int i=114; i<=133 ; i++){
            list.add(new Model_Emoji(i, new ImageIcon(getClass().getResource("/emoji/icon/"+i+".png"))).toSize(70, 72));
         
         }
         return list;
   }
   public List<Model_Emoji>getStyle4(){
         List<Model_Emoji> list = new ArrayList<>();
         for(int i=134; i<=151 ; i++){
            list.add(new Model_Emoji(i, new ImageIcon(getClass().getResource("/emoji/icon/"+i+".png"))).toSize(70, 72));
         
         }
         return list;
   }
   public Model_Emoji getEmoji(int id){
       
       if(id <= 89){
       return new Model_Emoji(id, new ImageIcon(getClass().getResource("/emoji/icon/"+id+".png"))).toSize(40, 40);//set cỡ nhỏ cho icon facebook
       }else{
       return new Model_Emoji(id, new ImageIcon(getClass().getResource("/emoji/icon/"+id+".png"))).toSize(140, 140);//set cỡ lớn cho emoji
       }
   }
}
