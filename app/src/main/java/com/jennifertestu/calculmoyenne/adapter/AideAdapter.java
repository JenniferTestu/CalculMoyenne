package com.jennifertestu.calculmoyenne.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.jennifertestu.calculmoyenne.R;

public class AideAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;

    private int[] imagesArray ={R.drawable.aide1,R.drawable.aide2,R.drawable.aide3,R.drawable.aide4,R.drawable.aide5};
    private String[] titresArray ={"Avec vous, quel que soit le niveau d’étude"
            ,"Surveillez vos moyennes"
            ,"Répertoriez vos notes"
            ,"Partagez vos notes"
            ,"Sauvegardez vos données"
    };
    private String[] detailsArray ={"Gérez vos années d’étude que vous soyez au collège, lycée, ou en études supérieures."
            ,"Ajoutez les matières ainsi que leur coefficient et gardez un œil sur chaque moyenne, y compris la moyenne générale."
            ,"Que ce soit un écrit, un oral, un TP ou un devoir, ajoutez vos notes /20 tout au long de l’année. Si les notes ont des poids différents dans la matière, il y a une option pour ça."
            ,"Envie de communiquer une note ou une moyenne à vos amis ou votre famille ? Partagez la nouvelle."
            ,"Il est possible de sauvegarder toutes vos notes et de les restaurer sur un autre appareil."
    };


    public AideAdapter(Context context) {
        this.context=context;
    }

    @Override
    public int getCount() {
        return titresArray.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view==object );
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }

    public Object instantiateItem(ViewGroup container, int position){
        layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.adapter_aide,null);

        pl.droidsonroids.gif.GifImageView imageView = (pl.droidsonroids.gif.GifImageView)view.findViewById(R.id.aide_img);
        TextView titreView = (TextView)view.findViewById(R.id.aide_titre);
        TextView descView = (TextView)view.findViewById(R.id.aide_desc);

        imageView.setImageResource(imagesArray[position]);
        titreView.setText(titresArray[position]);
        descView.setText(detailsArray[position]);

        container.addView(view);
        return view;
    }
}
